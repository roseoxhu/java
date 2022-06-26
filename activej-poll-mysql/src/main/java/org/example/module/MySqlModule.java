package org.example.module;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

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

    private MySqlModule() {

    }

    public static MySqlModule create() {
        return new MySqlModule();
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
                statement.execute(new String(loadResource(), UTF_8));
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
