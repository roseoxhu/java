package org.example.module;

import java.io.IOException;
import java.io.InputStream;

import javax.sql.DataSource;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;

/**
 * This module provides a MySql {@link DataSource}
 * <p>
 * It requires a 'mysql.properties' file to be stored in a classpath.
 *
 */
//@Slf4j
public class MySqlModule extends AbstractModule {
    public static final String MYSQL_PROPERTIES = "/mysql.properties";
    public static final String INIT_SCRIPT = "/init.sql";
    // Caused by: java.io.IOException: Could not find resource /mybatis-config.xml
    public static final String MYBATIS_CONFIG = "mybatis-config.xml";
    private MySqlModule() {

    }

    public static MySqlModule create() {
        return new MySqlModule();
    }

    // https://mybatis.org/mybatis-3/zh/getting-started.html
    // 作用域（Scope）和生命周期: SqlSessionFactory 一旦被创建就应该在应用的运行期间一直存在
    @Provides
    SqlSessionFactory sqlSessionFactory() throws IOException {
        try (InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG)) {
            //System.out.println(new String(inputStream.readAllBytes()));
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            SqlSessionFactory factory = builder.build(inputStream);
            return factory;
        }
    }

//    @Provides
//    DataSource dataSource() throws IOException, SQLException {
//        InputStream stream = getClass().getResourceAsStream(MYSQL_PROPERTIES);
//        if (stream == null) {
//            throw new RuntimeException("Create a 'mysql.properties' file " +
//                    "and add it to resources directory");
//        }
//
//        Properties properties = new Properties();
//        properties.load(stream);
//
//        MysqlDataSource dataSource = new MysqlDataSource();
//        dataSource.setUrl(properties.getProperty("jdbc.url")); // explicit
//        dataSource.setUser(properties.getProperty("jdbc.user"));
//        dataSource.setPassword(properties.getProperty("jdbc.password"));
//        dataSource.setServerTimezone(properties.getProperty("jdbc.timeZone"));
//        dataSource.setAllowMultiQueries(true);
//
//        log.info("jdbc.url={}", dataSource.getUrl());
//        log.info("jdbc.timeZone={}", dataSource.getServerTimezone());
//        //initialize(dataSource);
//
//        return dataSource;
//    }
//
//    private void initialize(MysqlDataSource dataSource) throws SQLException, IOException {
//        try (Connection connection = dataSource.getConnection()) {
//            try (Statement statement = connection.createStatement()) {
//                statement.execute(new String(loadResource(), UTF_8));
//                statement.execute("TRUNCATE TABLE poll");
//            }
//        }
//    }
//
//    private static byte[] loadResource() throws IOException {
//        /*try (InputStream stream = MySqlModule.class.getResourceAsStream(INIT_SCRIPT)) {
//            assert stream != null;
//            return stream.readAllBytes(); // JDK9+
//        }*/
//        URL url = MySqlModule.class.getResource(INIT_SCRIPT);
//        assert url != null;
//        return Files.readAllBytes(FileSystems.getDefault().getPath(url.getFile()));
//    }
}
