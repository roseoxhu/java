package org.example.pojo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 1.实体类和表的映射关系
 *      @Entity
 *      @Table
 * 2.类中属性和表中字段的映射关系
 *      @Id
 *      @GeneratedValue
 *      @Column
 *
 * 当属性名满足驼峰命名法时，可以不写@Colum注解
 */
@Entity
@Data
@Accessors(chain = true)
@Table(name = "poll_vote")
@IdClass(PollVoteId.class) // @IdClass vs @EmbeddedId: 没有独立的业务含义时用EmbeddedId, 有独立业务含义时用IdClass
public class PollVote implements Serializable {
    private static final long serialVersionUID = -5712693843887957056L;

    // composite key: 复合主键
    // No identifier specified for entity: org.example.pojo.PollVote
    // primary key (poll_id, option)
    @Id
    @Column(name = "poll_id")
    private int pollId; // =poll.id

    @Id
    // java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax;
    @Column(name = "`option`") // 字段名可能用了MySQL保留字，需要转义
    private String option; // the option for vote

    @Column(name = "votes_count")
    private int votesCount;
}
