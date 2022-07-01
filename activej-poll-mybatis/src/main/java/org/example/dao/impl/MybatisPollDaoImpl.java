package org.example.dao.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.example.dao.PollDao;
import org.example.pojo.Poll;
import org.example.pojo.PollVote;
import org.jetbrains.annotations.Nullable;

import io.activej.common.Utils;
import io.activej.promise.Promise;

/**
 * Implementation of {@link PollDao} which uses generic SQL commands for operation
 *
 * @see https://github.com/activej/activej/discussions/193
 * @see https://mybatis.org/mybatis-3/zh/getting-started.html
 */
//[START EXAMPLE]
public final class MybatisPollDaoImpl extends MybatisBaseDaoImpl implements PollDao {
    private final SqlSessionFactory sqlSessionFactory;
    private final Executor executor;

    public MybatisPollDaoImpl(SqlSessionFactory sqlSessionFactory, Executor executor) {
        super(sqlSessionFactory, executor);
        this.sqlSessionFactory = sqlSessionFactory;
        this.executor = executor;
    }

    @Override
    public Promise<@Nullable Poll> find(int id) {
        return Promise.ofBlocking(executor, () -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                Poll poll = sqlSession.selectOne("find", id);
                if (poll != null) {
                    // 查找投票的选项计数列表
                    List<PollVote> pollVoteList = sqlSession.selectList("findVote", id);

                    if (pollVoteList != null && !pollVoteList.isEmpty()) {
                        pollVoteList.forEach(item -> {
                            poll.getOptionsToVote().put(
                                    item.getOption(),
                                    item.getVotesCount());
                        });
                    }
                }

                return poll;
            }
        });
    }

    @Override
    public Promise<Map<Integer, Poll>> findAll() {
        return Promise.ofBlocking(executor, () -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                Map<Integer, Poll> result = new LinkedHashMap<>();

                // SELECT * FROM poll
                // No constructor found in org.example.pojo.Poll matching
                // [java.lang.Integer, java.lang.String, java.lang.String, java.sql.Timestamp]
                List<Poll> pollList = sqlSession.selectList("findAll");
                pollList.forEach(item -> {
                    Poll poll = new Poll(item.getTitle(), item.getMessage(), Utils.listOf());
                    poll.setId(item.getId());
                    result.put(item.getId(), poll);
                });

                // 获取投票选项计数
                // TODO Optimize n+1 SQL query issues
                for (Map.Entry<Integer, Poll> poll: result.entrySet()) {
                    List<PollVote> pollVoteList = sqlSession.selectList("findVote", poll.getKey());

                    if (pollVoteList != null && !pollVoteList.isEmpty()) {
                        pollVoteList.forEach(item -> {
                            poll.getValue().getOptionsToVote().put(
                                    item.getOption(),
                                    item.getVotesCount());
                        });
                    }
                }
                return result;
            }
        });
    }

    @Override
    public Promise<Integer> add(Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                sqlSession.insert("add", poll);

                // auto increment poll.id
                int pollId = poll.getId(); // keyProperty="id"
                if (pollId > 0) {
                    for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                        sqlSession.insert("addVote", Map.of("pollId", pollId,
                                "option", entry.getKey())); // 构造Map传值
                    }
                }
                sqlSession.commit();

                return pollId;
            }
        });
    }

    @Override
    public Promise<Boolean> update(int id, Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                sqlSession.update("update", poll);

                for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                    PollVote pollVote = new PollVote();
                    pollVote.setPollId(id)
                        .setOption(entry.getKey())
                        .setVotesCount(entry.getValue());
                    sqlSession.update("updateVote", pollVote);
                }
                // 在 MyBatis 中配置为JDBC类型的事务管理器，进行插入，更新，删除操作时，要显示的提交事务。
                // JdbcTransaction - Rolling back JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@779c3b4b]
                sqlSession.commit();
                //sqlSession.close();

                return true;
            }
        });
    }

    @Override
    public Promise<Boolean> remove(int id) {
        return Promise.ofBlocking(executor, () -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                sqlSession.delete("remove", id);
                sqlSession.delete("removeVote", id);
                sqlSession.commit();
                return true;
            }
        });
    }

}
//[END EXAMPLE]