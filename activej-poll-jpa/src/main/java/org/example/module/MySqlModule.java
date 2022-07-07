package org.example.module;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;

import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;
import lombok.extern.slf4j.Slf4j;

/**
 * This module provides a MySql {@link DataSource}
 * <p>
 * It requires a 'mysql.properties' file to be stored in a classpath.
 *
 */
@Slf4j
public class MySqlModule extends AbstractModule {
    public static final String MYSQL_PROPERTIES = "/mysql.properties";
    public static final String INIT_SCRIPT = "/init.sql";
    public static final String PERSISTENCE_UNIT_NAME = "hibernatejpa";

    private MySqlModule() {

    }

    public static MySqlModule create() {
        return new MySqlModule();
    }


    @Provides
    EntityManager entityManager() {
        //1.加载配置文件创建工厂（实体类工厂）对象
        EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        //2.通过实体管理器工厂获取实体管理器
        EntityManager entityManager = factory.createEntityManager();
        return entityManager;
    }

    @Provides
    DataSource dataSource() throws IOException, SQLException {
        InputStream stream = getClass().getResourceAsStream(MYSQL_PROPERTIES);
        if (stream == null) {
            throw new RuntimeException("Create a 'mysql.properties' file " +
                    "and add it to resources directory");
        }

        Properties properties = new Properties();
        properties.load(stream);

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(properties.getProperty("jdbc.url")); // explicit
        dataSource.setUser(properties.getProperty("jdbc.user"));
        dataSource.setPassword(properties.getProperty("jdbc.password"));
        dataSource.setServerTimezone(properties.getProperty("jdbc.timeZone"));
        dataSource.setAllowMultiQueries(true);

        log.info("jdbc.url={}", dataSource.getUrl());
        log.info("jdbc.timeZone={}", dataSource.getServerTimezone());
        //initialize(dataSource);

        return dataSource;
    }

    private void initialize(MysqlDataSource dataSource) throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(new String(loadResource(), StandardCharsets.UTF_8));
                statement.execute("TRUNCATE TABLE poll");
            }
        }
    }

    private static byte[] loadResource() throws IOException {
        /*try (InputStream stream = MySqlModule.class.getResourceAsStream(INIT_SCRIPT)) {
            assert stream != null;
            return stream.readAllBytes(); // JDK9+
        }*/
        URL url = MySqlModule.class.getResource(INIT_SCRIPT);
        assert url != null;
        return Files.readAllBytes(FileSystems.getDefault().getPath(url.getFile()));
    }
}
