package org.example.dao.impl;

import java.util.concurrent.Executor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.example.dao.BaseDao;
import org.example.dao.PollDao;
import org.jetbrains.annotations.NotNull;

import io.activej.promise.Promise;

/**
 * Implementation of {@link PollDao} which uses generic SQL commands for operation
 *
 * @see https://github.com/activej/activej/discussions/193
 * @see https://mybatis.org/mybatis-3/zh/getting-started.html
 */
//[START EXAMPLE]
public class MybatisBaseDaoImpl implements BaseDao {
    private final SqlSessionFactory sqlSessionFactory;
    private final Executor executor;

    public MybatisBaseDaoImpl(SqlSessionFactory sqlSessionFactory, Executor executor) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.executor = executor;
    }

    @Override
    public @NotNull String now() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            BaseDao baseDao = sqlSession.getMapper(BaseDao.class); // XyzMapper.class

            return baseDao.now();
        }
    }

    @Override
    public Promise<@NotNull String> asyncNow() {
        return Promise.ofBlocking(executor, () -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                //BaseDao baseDao = sqlSession.getMapper(BaseDao.class);
                //return baseDao.asyncNow();
                return sqlSession.selectOne("asyncNow");
            }
        });
    }

}
//[END EXAMPLE]