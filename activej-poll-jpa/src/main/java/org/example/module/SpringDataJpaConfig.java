package org.example.module;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.example.util.HikariConfigConverter;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

import io.activej.config.Config;

// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.java-config
// 配置jpa
@Configuration
//@ComponentScan(basePackages = {"org.example"})
// 指定Repository所在的包
// No qualifying bean of type 'org.example.repository.PollRepository' available
@EnableJpaRepositories(basePackages = {"org.example"})
@EnableTransactionManagement
public class SpringDataJpaConfig {
    public static final String MYSQL_PROPERTIES = "/mysql-pooled.properties";

    @Bean
    public DataSource dataSource() {
//        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
//        return builder.setType(EmbeddedDatabaseType.HSQL).build();
//
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
//        dataSource.setUsername("username");
//        dataSource.setPassword("password");
//        return dataSource;

        HikariConfigConverter converter = HikariConfigConverter.create().withAllowMultiQueries();
        Config config = Config.create()
                .overrideWith(Config.ofClassPathProperties(MYSQL_PROPERTIES, true));
        return new HikariDataSource(config.get(converter, "hikari"));
        // Creating shared instance of singleton bean 'dataSource'
    }


    // 名字必须是entityManagerFactory,或者把@bean中name属性设置为entityManagerFactory
    //public EntityManagerFactory entityManagerFactory() {
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        // LocalContainerEntityManagerFactoryBean - Building JPA container EntityManagerFactory for persistence unit 'default'
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        // Unable to create requested service [org.hibernate.engine.jdbc.env.spi.JdbcEnvironment]
        // 设置数据库(如果在hibernate中配置了连接池,则不需要设置 [hibernate.connection.provider_class])
        emf.setDataSource(dataSource); // Non JTA datasource
        emf.setJtaDataSource(dataSource);

        // 指定Entity所在的包
        //emf.setPackagesToScan(new String[] {"org.example"});
        emf.setPackagesToScan("org.example");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        //emf.setPersistenceProvider(new HibernatePersistenceProvider()); //SpringHibernateJpaPersistenceProvider

        // 配置属性 @see org.hibernate.cfg.AvailableSettings
        Properties properties = new Properties();
//        properties.setProperty("javax.persistence.provider", "org.hibernate.jpa.HibernatePersistenceProvider");
//        properties.setProperty("hibernate.connection.driver_class", "oracle.jdbc.OracleDriver");
//        properties.setProperty("hibernate.connection.url", "jdbc:oracle:thin:@192.168.0.21:1521:test");
//        properties.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
//        properties.setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/test?autoReconnect=true&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8");
//        properties.setProperty("hibernate.connection.provider_class", "com.zaxxer.hikari.hibernate.HikariConnectionProvider");
//        properties.setProperty("hibernate.connection.username", "root");
//        properties.setProperty("hibernate.connection.password", "123456");
        //properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        //properties.setProperty("hibernate.hbm2ddl.auto", "update"); // 实体类中的修改会同步到数据库表结构中，慎用。
        //properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "true");
        // java.sql.SQLSyntaxErrorException: Table 'test.poll_poll_vote' doesn't exist
        properties.setProperty("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        emf.setJpaProperties(properties);
        return emf;
    }

    // 名字必须是transactionManager,或者把@bean中name属性设置为transactionManager
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
        // Creating shared instance of singleton bean 'transactionManager'
    }

}
