package org.example.dao.impl;

import java.util.concurrent.Executor;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.example.dao.BaseDao;
import org.jetbrains.annotations.NotNull;

import io.activej.promise.Promise;

public class HibernateJpaBaseDaoImpl implements BaseDao {
    private final EntityManager entityManager;
    private final Executor executor;

    public HibernateJpaBaseDaoImpl(EntityManager entityManager, Executor executor) {
        this.entityManager = entityManager;
        this.executor = executor;
    }

    @Override
    public @NotNull String now() {
        Query query = entityManager.createNativeQuery("SELECT now() FROM DUAL");
        return query.getSingleResult().toString();
    }

    @Override
    public Promise<@NotNull String> asyncNow() {
        return Promise.ofBlocking(executor, () -> {
            //java.util.ConcurrentModificationException: null
            // java.sql.SQLException: Operation not allowed after ResultSet closed
            //return now();

            // 增加数据连接关闭方法，当从dataSource获取的连接使用完成后，调用close方法，避免数据源对象仍然可用，造成连接泄露
            // 空闲时间idleTimeout生效，减少空闲连接占用，尽快释放数据库连接
            // 生命周期maxLifetime值设为10分钟，尽快释放数据库无效连接
            // TODO 排查高并发情况下（没有正确处理多线程连接共享对象），貌似连接使用完成后，没有关闭（正常返回连接池）
            Query query = entityManager.createNativeQuery("SELECT now() FROM DUAL");
            final String now = query.getSingleResult().toString();
            return now;
        });
    }

}
