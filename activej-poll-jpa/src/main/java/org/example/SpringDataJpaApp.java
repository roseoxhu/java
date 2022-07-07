package org.example;

import org.example.module.SpringDataJpaConfig;
import org.example.repository.PollRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// 1. 引入依赖:
//   - spring-data-jpa,
//   - hibernate-core,
//   - HikariCP,
//   - mysql-connector-java
// 2. 配置jpa
// 3. 编写实体类和Repository
//   - @Entity,@Repository
//   - XyzRepository extends JpaRepository<>
// 4. 使用 jpa

// @ComponentScan(basePackageClasses = {AppConfig.class, SpringDataJpaConfig.class})
public class SpringDataJpaApp {
    private static final Logger log = LoggerFactory.getLogger(SpringDataJpaApp.class);

    public static void main(String[] args) {
        log.info("Start spring ....");
        @SuppressWarnings("resource")
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringDataJpaConfig.class);
        // 获取repository
        PollRepository repo = context.getBean(PollRepository.class);

        log.info("Jpa now="+ repo.now());
        log.info("Jpa findByTitleLike="+ repo.findByTitleLike("%事件%"));

    }

}
