package org.example.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.example.dao.PollDao;
import org.example.pojo.Poll;
import org.jetbrains.annotations.Nullable;

import io.activej.common.Utils;
import io.activej.promise.Promise;

/**
 * Implementation of {@link PollDao} which uses generic SQL commands for operation
 *
 * @see https://github.com/activej/activej/discussions/193
 */
//[START EXAMPLE]
public final class MySqlPollDaoImpl extends MySqlBaseDaoImpl implements PollDao {
    private final DataSource dataSource;
    private final Executor executor;

    public MySqlPollDaoImpl(DataSource dataSource, Executor executor) {
        super(dataSource, executor);
        this.dataSource = dataSource;
        this.executor = executor;
    }

    @Override
    public Promise<@Nullable Poll> find(int id) {
        return Promise.ofBlocking(executor, () -> {
            try (Connection connection = dataSource.getConnection()) {
                Poll poll = null;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT id,title,message FROM poll WHERE id=?")) {
                    statement.setInt(1, id);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            return null;
                        }

                        String title = resultSet.getString(2);
                        String message = resultSet.getString(3);
                        poll = new Poll(title, message, Utils.listOf());
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT `option`,votes_count FROM poll_vote WHERE poll_id=?")) {
                    statement.setInt(1, id);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            poll.getOptionsToVote().put(
                                    resultSet.getString("option"),
                                    resultSet.getInt("votes_count"));
                        }
                        return poll;
                    }
                }
            }
        });
    }

    @Override
    public Promise<Map<Integer, Poll>> findAll() {
        return Promise.ofBlocking(executor, () -> {
            try (Connection connection = dataSource.getConnection()) {
                Map<Integer, Poll> result = new LinkedHashMap<>();
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM poll")) {
                    try (ResultSet resultSet = statement.executeQuery()) {

                        while (resultSet.next()) {
                            int id = resultSet.getInt("id");
                            String title = resultSet.getString("title");
                            String message = resultSet.getString("message");

                            Poll poll = new Poll(title, message, Utils.listOf());
                            //poll.setId(id);
                            result.put(id, poll);
                        }
                    }
                }

                // 获取投票选项计数
                for (Map.Entry<Integer, Poll> poll: result.entrySet()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT `option`,votes_count FROM poll_vote WHERE poll_id=?")) {
                        statement.setInt(1, poll.getKey());
                        try (ResultSet resultSet = statement.executeQuery()) {
                            while (resultSet.next()) {
                                poll.getValue().getOptionsToVote().put(
                                        resultSet.getString("option"),
                                        resultSet.getInt("votes_count"));
                            }
                        }
                    }
                }
                return result;
            }
        });
    }

    @Override
    public Promise<Integer> add(Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                connection = dataSource.getConnection();
                // 关闭自动提交，同时开启事务
                connection.setAutoCommit(false);
                statement = connection.prepareStatement(
                        "INSERT INTO poll(title, message) VALUES(?, ?)");

                statement.setString(1, poll.getTitle());
                statement.setString(2, poll.getMessage());
                statement.executeUpdate();

                // auto increment poll.id
                int pollId = 0;
                ResultSet resultSet = statement.executeQuery("SELECT last_insert_id()");
                if (resultSet.next()) {
                    pollId = resultSet.getInt(1);

                    for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                        statement = connection.prepareStatement(
                                "INSERT INTO poll_vote(poll_id, `option`) VALUES(?, ?)");

                        statement.setInt(1, pollId);
                        statement.setString(2, entry.getKey());
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                return pollId;
            } catch (Exception e) {
                connection.rollback();
                e.printStackTrace();
            } finally {
                if (connection != null) connection.close();
                if (statement != null) statement.close();
            }
            return 0;
        });
    }

    @Override
    public Promise<Boolean> update(int id, Poll poll) {
        return Promise.ofBlocking(executor, () -> {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE poll SET title=?, message=? WHERE id=?")) {

                    statement.setString(1, poll.getTitle());
                    statement.setString(2, poll.getMessage());
                    statement.setInt(3, id);

                    //return statement.executeUpdate() != 0;
                    statement.executeUpdate();
                }

                for (Map.Entry<String, Integer> entry : poll.getOptions()) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE poll_vote SET votes_count=? WHERE poll_id=? and `option`=?")) {

                        statement.setInt(1, entry.getValue());
                        statement.setInt(2, id);
                        statement.setString(3, entry.getKey());

                        //return statement.executeUpdate() != 0;
                        statement.executeUpdate();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public Promise<Boolean> remove(int id) {
        return Promise.ofBlocking(executor, () -> {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM poll WHERE id=?")) {

                    statement.setInt(1, id);

                    //return statement.executeUpdate() != 0;
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM poll_vote WHERE poll_id=?")) {

                    statement.setInt(1, id);

                    return statement.executeUpdate() != 0;
                }
            }
        });
    }

}
//[END EXAMPLE]