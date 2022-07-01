package org.example.dao;

import org.jetbrains.annotations.NotNull;

import io.activej.promise.Promise;

/**
 * Basic DAO (Data Access Object) class that provides common DB-operations
 */
//[START EXAMPLE]
public interface BaseDao {
    @NotNull String now();
    Promise<@NotNull String> asyncNow();

}
//[END EXAMPLE]