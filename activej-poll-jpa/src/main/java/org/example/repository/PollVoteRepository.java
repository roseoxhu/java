package org.example.repository;

import java.util.List;

import org.example.pojo.PollVote;
import org.example.pojo.PollVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// 实现 JpaRepository 接口，生成基本的 CRUD 操作样板代码。
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
/**
 * 符合Spring Data Jpa 的 dao层接口规范
 *      JpaRepository<操作的实体类类型，实体类中主键属性的类型>
 *          #封装了基本CRUD操作
 *      JpaSpecificationExecutor <操作的实体类类型>
 *          #封装了复杂查询（分页）
 */
@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, PollVoteId>, MyBaseRepository<PollVote, PollVoteId> {
    // Error creating bean with name 'pollVoteRepository' defined in org.example.repository.PollVoteRepository
    // defined in @EnableJpaRepositories declared on SpringDataJpaConfig: Invocation of init method failed;
    // nested exception is java.lang.IllegalArgumentException: This class [class org.example.pojo.PollVote] does not define an IdClass

    List<PollVote> findAllByPollId(int pollId);

    @Modifying
    @Transactional //JPA 执行update/delete query 需要加上事务
    @Query(value = "DELETE FROM poll_vote WHERE poll_id=?", nativeQuery = true)
    void deleteAllByPollId(int pollId);
}
