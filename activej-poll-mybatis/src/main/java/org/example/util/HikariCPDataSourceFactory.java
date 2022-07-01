package org.example.util;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;

import com.zaxxer.hikari.HikariDataSource;

import io.activej.config.Config;

public class HikariCPDataSourceFactory implements DataSourceFactory {
//public class HikariCPDataSourceFactory extends UnpooledDataSourceFactory {

    public static final String MYSQL_PROPERTIES = "mysql-pooled.properties";
    private DataSource dataSource;

    @Override
    public void setProperties(Properties props) {

    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }

    public HikariCPDataSourceFactory() {
        //File file = new File(MYSQL_PROPERTIES);
        //HikariConfig config = new HikariConfig(file.getPath());
        //this.dataSource = new HikariDataSource(config);

        HikariConfigConverter converter = HikariConfigConverter.create().withAllowMultiQueries();
        Config config = Config.create()
                .overrideWith(Config.ofClassPathProperties(MYSQL_PROPERTIES, true));
        this.dataSource = new HikariDataSource(config.get(converter, "hikari"));
    }
}