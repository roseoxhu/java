package org.example.dao;

import java.util.Map;

import org.example.pojo.Poll;

import io.activej.promise.Promise;

//[START EXAMPLE]
public interface PollDao {
    Promise<Poll> find(int id);
    Promise<Map<Integer, Poll>> findAll();
    Promise<Integer> add(Poll poll);
    Promise<Void> update(int id, Poll poll);
    Promise<Void> remove(int id);

}
//[END EXAMPLE]