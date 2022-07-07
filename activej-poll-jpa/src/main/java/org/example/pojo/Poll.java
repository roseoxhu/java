package org.example.pojo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

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
@Table(name = "poll")
public class Poll {
    /**
     * mysql数据库中,只有使用GenerationType.IDENTITY注解
     * 才能正常使用数据库的自动增长特性
     */
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Getter
    private /* final */String title;
    @Getter
    private /* final */String message;
    @Getter
    @Transient // 设置不需要持久化
    private /* final */Map<String, Integer> optionsToVote;

    //@Getter
    //@OneToMany(targetEntity = PollVote.class, fetch = FetchType.EAGER)
    //@JoinTable(name = "poll") // Table 'test.poll_poll_vote' doesn't exist
    //private List<PollVote> pollVoteList;

    // No default constructor for entity: org.example.pojo.Poll
    public Poll() {
        this.optionsToVote = new LinkedHashMap<>();
    }

    public Poll(String title, String message) {
        this.title = title;
        this.message = message;
        this.optionsToVote = new LinkedHashMap<>();
    }

    public Poll(Integer id, String title, String message) {
        this(title, message);
        this.id = id;
    }

    public Poll(String title, String message, List<String> options) {
        this(title, message);

        for (String option : options) {
            optionsToVote.put(option, 0);
        }
    }

    public Poll vote(String option) {
        Integer votesCount = optionsToVote.get(option);
        optionsToVote.put(option, ++votesCount);
        return this;
    }

    public Set<Map.Entry<String, Integer>> getOptions() {
        return optionsToVote.entrySet();
    }

}
