package org.example.repository;

import java.util.List;

import org.example.pojo.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
public interface PollRepository extends JpaRepository<Poll, Integer>, MyBaseRepository<Poll, Integer> {
    /**
     * 方法名称规则查询
     *      是对jpql查询，更加深入的一层封装，我们只需要按照SpringDataJpa提供的方法名称
     *      规则定义方法，不需要再去配置jpql语句，完成查询
     *  SpringDataJpa 的运行阶段会根据方法名称进行解析,
     *      findBy + 实体类属性名称（条件查询）
     *      findBy + 实体类属性名称 + 查询方式（like，is null,between等）
     *      findBy + 实体类属性名 + 查询方式 + 多条件连接符（and|or） +  实体类属性 + 查询方式
     *              模糊匹配需要写查询方式，精准匹配可以不写
     */
    List<Poll> findByTitleLike(String title);
    Integer countByTitle(String title);

    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.custom-implementations
    Poll findById(int id);
    List<Poll> findAll();

}
