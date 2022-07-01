package org.example.pojo;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PollVote implements Serializable {
    private static final long serialVersionUID = -5712693843887957056L;

    private int pollId; // =poll.id
    private String option; // the option for vote
    private int votesCount;
}
