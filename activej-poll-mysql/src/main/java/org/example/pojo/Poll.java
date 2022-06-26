package org.example.pojo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

public class Poll {
    @Getter
    @Setter
    private Integer id;
    @Getter
    private final String title;
    @Getter
    private final String message;
    @Getter
    private final Map<String, Integer> optionsToVote;

    public Poll(String title, String message, List<String> options) {
        this.title = title;
        this.message = message;
        this.optionsToVote = new LinkedHashMap<>();

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
