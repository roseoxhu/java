package org.example.dao.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.example.dao.PollDao;
import org.example.pojo.Poll;

//[START EXAMPLE]
public final class PollDaoImpl implements PollDao {
    private final Map<Integer, Poll> polls = new HashMap<>();
    private final Random random = new Random();


    @Override
    public Poll find(int id) {
        return polls.get(id);
    }

    @Override
    public Map<Integer, Poll> findAll() {
        return polls;
    }

    @Override
    public int add(Poll poll) {
        int range = Integer.MAX_VALUE;
        int id = random.nextInt(range);
        polls.put(id, poll);
        return id;
    }

    @Override
    public void update(int id, Poll poll) {
        polls.put(id, poll);
    }

    @Override
    public void remove(int id) {
        polls.remove(id);
    }
}
//[END EXAMPLE]