package org.example.pojo;

import java.io.Serializable;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 复合主键 https://www.objectdb.com/java/jpa/entity/id
// Entity "class org.example.pojo.PollVote" is using composite primary key comprising
// more than one fields "[option, pollId]", but no primary key class has been defined
// in this class or any of its persistent super classes.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollVoteId implements Serializable {
    /**
     * [WARNING] The composite identity class "class org.example.pojo.PollVoteId"
     * for entity "class org.example.pojo.PollVote" is not serializable.
     */
    private static final long serialVersionUID = 2191148277696682020L;

    // java.sql.SQLException: Field 'pollId' doesn't have a default value
    @Column(name = "poll_id")
    private int pollId;

// org.hibernate.SQL -
//    insert
//    into
//        poll_vote
//        (votes_count, option, poll_id)
//    values
//        (?, ?, ?)
    // java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near 'option, poll_id) values (0, '正', 12)' at line 1
    // 还是字段命名惹的祸，option为MySQL保留字需要转义
    @Column(name = "`option`")
    private String option;
}
