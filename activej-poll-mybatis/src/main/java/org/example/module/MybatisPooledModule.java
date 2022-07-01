package org.example.module;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import io.activej.inject.annotation.Named;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;

public class MybatisPooledModule extends AbstractModule {
    public static final String MYSQL_PROPERTIES = "mysql-pooled.properties";
    // Caused by: java.io.IOException: Could not find resource /mybatis-config.xml
    public static final String MYBATIS_CONFIG = "mybatis-config-hikari.xml";

    private MybatisPooledModule() {

    }

    public static MybatisPooledModule create() {
        return new MybatisPooledModule();
    }

    // https://mybatis.org/mybatis-3/zh/getting-started.html
    // 作用域（Scope）和生命周期: SqlSessionFactory 一旦被创建就应该在应用的运行期间一直存在
    @Provides
    @Named("hikari")
    SqlSessionFactory sqlSessionFactory() throws IOException {
        try (InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG)) {
            //System.out.println(new String(inputStream.readAllBytes()));
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            SqlSessionFactory factory = builder.build(inputStream);
            return factory;
        }
    }

//    @Provides
//    @Named("pooled")
//    Config config() {
//        return Config.create()
//                .overrideWith(Config.ofClassPathProperties(MYSQL_PROPERTIES, true))
//                .overrideWith(Config.ofSystemProperties("config"));
//    }
//
//    @Provides
//    @Named("hikari")
//    DataSource hikariDataSource(@Named("pooled") Config config) {
//        HikariConfigConverter converter = HikariConfigConverter.create().withAllowMultiQueries();
//        return new HikariDataSource(config.get(converter, "hikari"));
//    }

}
