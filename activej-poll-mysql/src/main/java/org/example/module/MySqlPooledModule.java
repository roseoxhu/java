package org.example.module;

import javax.sql.DataSource;

import org.example.util.HikariConfigConverter;

import com.zaxxer.hikari.HikariDataSource;

import io.activej.config.Config;
import io.activej.inject.annotation.Named;
import io.activej.inject.annotation.Provides;
import io.activej.inject.module.AbstractModule;

public class MySqlPooledModule extends AbstractModule {
    public static final String MYSQL_PROPERTIES = "/mysql-pooled.properties";
    public static final String INIT_SCRIPT = "init.sql";

    private MySqlPooledModule() {

    }

    public static MySqlPooledModule create() {
        return new MySqlPooledModule();
    }

    @Provides
    @Named("pooled")
    Config config() {
        return Config.create()
                .overrideWith(Config.ofClassPathProperties(MYSQL_PROPERTIES, true))
                .overrideWith(Config.ofSystemProperties("config"));
    }

    @Provides
    @Named("hikari")
    DataSource hikariDataSource(@Named("pooled") Config config) {
        HikariConfigConverter converter = HikariConfigConverter.create().withAllowMultiQueries();
        return new HikariDataSource(config.get(converter, "hikari"));
    }
}
