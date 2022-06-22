package org.example.dao;

import java.util.Map;

import org.example.pojo.Poll;

//[START EXAMPLE]
public interface PollDao {
    Poll find(int id);
    Map<Integer, Poll> findAll();
    int add(Poll poll);
    void update(int id, Poll poll);
    void remove(int id);

}
//[END EXAMPLE]