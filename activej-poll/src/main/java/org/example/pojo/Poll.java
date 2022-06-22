package org.example.pojo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

public class Poll {
    @Getter
    private final String title;
    @Getter
    private final String message;
    private final Map<String, Integer> optionsToVote;

    public Poll(String title, String message, List<String> options) {
        this.title = title;
        this.message = message;
        this.optionsToVote = new LinkedHashMap<>();

        for (String option : options) {
            optionsToVote.put(option, 0);
        }
    }

    public void vote(String option) {
        Integer votesCount = optionsToVote.get(option);
        optionsToVote.put(option, ++votesCount);
    }

    public Set<Map.Entry<String, Integer>> getOptions() {
        return optionsToVote.entrySet();
    }

}
