package org.example.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.example.dao.BaseDao;
import org.example.dao.PollDao;
import org.jetbrains.annotations.NotNull;

import io.activej.promise.Promise;

/**
 * Implementation of {@link PollDao} which uses generic SQL commands for operation
 *
 * @see https://github.com/activej/activej/discussions/193
 */
//[START EXAMPLE]
public class MySqlBaseDaoImpl implements BaseDao {
    private final DataSource dataSource;
    private final Executor executor;

    public MySqlBaseDaoImpl(DataSource dataSource, Executor executor) {
        this.dataSource = dataSource;
        this.executor = executor;
    }

    @Override
    public @NotNull String now() {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT now() FROM DUAL")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return resultSet.getString(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Promise<@NotNull String> asyncNow() {
        return Promise.ofBlocking(executor, () -> {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT now() FROM DUAL")) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            return null;
                        }

                        return resultSet.getString(1);
                    }
                }
            }
        });
    }

}
//[END EXAMPLE]