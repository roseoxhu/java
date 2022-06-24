package org.example.pojo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.activej.serializer.annotations.Serialize;
import lombok.Getter;
import lombok.Setter;

public class Poll implements Serializable {
    private static final long serialVersionUID = 5587815982134011140L;

    @Getter
    @Setter
    @Serialize
    public /* final */ String title;
    @Getter
    @Setter
    @Serialize
    public /* final */ String message;
    @Serialize
    public /* final */ Map<String, Integer> optionsToVote;

    //Exception in thread "main" io.activej.inject.binding.DIException: Failed to call method org.example.dao.PollDao org.example.PollApp.asyncPollRepo(io.activej.eventloop.Eventloop)
    //Caused by: java.lang.ExceptionInInitializerError
    //Caused by: java.lang.IllegalArgumentException: Constructor not found:org.example.pojo.Poll()
    public Poll() {

    }

    public Poll(String title, String message, List<String> options) {
        this.title = title;
        this.message = message;
        this.optionsToVote = new LinkedHashMap<>();

        for (String option : options) {
            optionsToVote.put(option, 0);
        }
    }

    //public void vote(String option) {
    public Poll vote(String option) { // 改进适合 Promise
        Integer votesCount = optionsToVote.get(option);
        optionsToVote.put(option, ++votesCount);
        return this;
    }

    public Set<Map.Entry<String, Integer>> getOptions() {
        return optionsToVote.entrySet();
    }

    @Override
    public String toString() {
        return "{\"title\": "+ title +", "
                + "\"message\": "+ message + ", "
                + "\"optionsToVote\": "+ optionsToVote + "}";
    }
}
