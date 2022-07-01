package org.example.dao;

import java.util.Map;

import org.example.pojo.Poll;
import org.jetbrains.annotations.Nullable;

import io.activej.promise.Promise;

/**
 * Basic DAO (Data Access Object) class that provides Poll CRUD-operations
 */
//[START EXAMPLE]
public interface PollDao extends BaseDao {
    Promise<@Nullable Poll> find(int id);
    Promise<Map<Integer, Poll>> findAll();
    Promise<Integer> add(Poll poll);
    Promise<Boolean> update(int id, Poll poll);
    Promise<Boolean> remove(int id);

//    Promise<Integer> addVote(int pollId, String option);
//    Promise<Boolean> updateVote(int pollId, String option, int votesCount);
//    Promise<Boolean> removeVote(int id);

}
//[END EXAMPLE]