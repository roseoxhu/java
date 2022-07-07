package org.example.dao.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.example.dao.PollDao;
import org.example.pojo.Poll;
import org.example.pojo.PollVote;
import org.jetbrains.annotations.Nullable;

import io.activej.common.Utils;
import io.activej.promise.Promise;

public class HibernateJpaPollDaoImpl extends HibernateJpaBaseDaoImpl implements PollDao {
    private final EntityManager entityManager;
    private final Executor executor;

    public HibernateJpaPollDaoImpl(EntityManager entityManager, Executor executor) {
        super(entityManager, executor);
        this.entityManager = entityManager;
        this.executor = executor;
    }

    // SELECT 标识符变量 FROM 实体名称 [AS] 标识符变量
    // @Query("SELECT E FROM Employee E")
    // 命名参数： : + 参数名称
    // 位置参数： ? + 参数编号
    @Override
    public Promise<@Nullable Poll> find(int id) {
        return Promise.ofBlocking(executor, () -> {
            Poll poll = entityManager.find(Poll.class, id);
            if (poll != null) {
                // use JPA-style ordinal parameters (e.g., `?1`)
                // 在sql中使用?i绑定方法上的第i个参数。
                // sql中的参数:param，绑定方法上用@Param("param")标记的参数。
                // 语法差别：
                // sql 数据库表名，字段名
                // jpql 实体POJO类名，成员变量名
                Query query = entityManager.createQuery("from PollVote where pollId=?1");
                query.setParameter(1, id); // 下标从 1 开始

                @SuppressWarnings("unchecked")
                List<PollVote> pollVoteList = (List<PollVote>)query.getResultList();
                if (pollVoteList != null && !pollVoteList.isEmpty()) {
                    pollVoteList.forEach(item -> {
                        poll.getOptionsToVote().put(
                                item.getOption(),
                                item.getVotesCount());
                    });
                }
            }
            return poll;
        });
    }

    @Override
    public Promise<Map<Integer, Poll>> findAll() {
        return Promise.ofBlocking(executor, () -> {
            Map<Integer, Poll> result = new LinkedHashMap<>();

            Query query = entityManager.createQuery("from Poll");

            @SuppressWarnings("unchecked")
            List<Poll> pollList = (List<Poll>)query.getResultList();
            pollList.forEach(item -> {
                Poll poll = new Poll(item.getTitle(), item.getMessage(), Utils.listOf());
                poll.setId(item.getId());
                result.put(item.getId(), poll);
            });

            for (Map.Entry<Integer, Poll> entry: result.entrySet()) {
                Query subquery = entityManager.createQuery("from PollVote where pollId=?1");
                subquery.setParameter(1, entry.getKey());

                @SuppressWarnings("unchecked")
                List<PollVote> pollVoteList = (List<PollVote>)subquery.getResultList();

                if (pollVoteList != null && !pollVoteList.isEmpty()) {
                    pollVoteList.forEach(item -> {
                        entry.getValue().getOptionsToVote().put(
                                item.getOption(),
                                item.getVotesCount());
                    });
                }
            }

            return result;
        });
    }

    // 插入数据的JPQL语法为：insert into EntityName properties_list select_statement
    // - EntityName表示持久化类的名字
    // - properties_list表示持久化类的属性列表
    // - select_statement表示子查询语句。
    // JPQL只支持insert into ... select ... 形式的插入语句，而不支持insert into ... values ... 形式的插入语句。
    @Override
    public Promise<Integer> add(Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            EntityTransaction tx = entityManager.getTransaction();
            tx.begin();
            // String jpqlInsert = "insert into Poll (title, message)"
            // + " select poll.title, poll.message from dual";
            //entityManager.createQuery(jpqlInsert).executeUpdate();
            entityManager.persist(poll);

            int pollId = poll.getId(); // auto increment poll.id
            if (pollId > 0) {
                for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                    PollVote pollVote = new PollVote();
                    pollVote.setPollId(pollId)
                        .setOption(entry.getKey())
                        .setVotesCount(entry.getValue());
                    entityManager.persist(pollVote);
                }
            }
            entityManager.flush();
            tx.commit();

            entityManager.clear();
            return pollId;
        });
    }

    @Override
    public Promise<Boolean> update(int id, Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

            // javax.persistence.TransactionRequiredException: Executing an update/delete query
            entityManager.getTransaction().begin();
            for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                CriteriaUpdate<PollVote> updateQuery = criteriaBuilder.createCriteriaUpdate(PollVote.class);
                Root<PollVote> root = updateQuery.from(PollVote.class);

                updateQuery.set("votesCount", entry.getValue())
                    .where(criteriaBuilder.equal(root.get("pollId"), id),
                            criteriaBuilder.equal(root.get("option"), entry.getKey()));
                //String jpql = "UPDATE PollVote SET votesCount=?1 WHERE pollId=?2 and option=?3";
                entityManager.createQuery(updateQuery).executeUpdate();
            }
            entityManager.flush();
            entityManager.getTransaction().commit();

            entityManager.clear(); // 持久化缓存失效，确保实体数据取到最新值，页面数据刷新！
            return true;
        });
    }

    @Override
    public Promise<Boolean> remove(int id) {
        return Promise.ofBlocking(executor, () -> {
            entityManager.getTransaction().begin();
            // String jpql = "DELETE FROM Poll WHERE id = ?1";
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaDelete<Poll> deleteQuery = criteriaBuilder.createCriteriaDelete(Poll.class);
            Root<Poll> root = deleteQuery.from(Poll.class);
            deleteQuery.where(criteriaBuilder.equal(root.get("id"), id));
            // javax.persistence.TransactionRequiredException: Executing an update/delete query
            /* int updated = */entityManager.createQuery(deleteQuery).executeUpdate();

            // String jpql = "DELETE FROM PollVote WHERE pollId = ?1";
            CriteriaDelete<PollVote> deleteQuery2 = criteriaBuilder.createCriteriaDelete(PollVote.class);
            Root<PollVote> root2 = deleteQuery2.from(PollVote.class);
            deleteQuery2.where(criteriaBuilder.equal(root2.get("pollId"), id));
            int updated2 = entityManager.createQuery(deleteQuery2).executeUpdate();
            entityManager.getTransaction().commit();
            return updated2 > 0;
        });
    }


}
