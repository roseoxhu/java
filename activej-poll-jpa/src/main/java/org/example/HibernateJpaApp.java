package org.example;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.example.pojo.Poll;

public class HibernateJpaApp {

    public static void main(String[] args) {
        final String persistenceUnitName = "hibernatejpa";
        //1.加载配置文件创建工厂（实体类工厂）对象
        EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnitName);
        //2.通过实体管理器工厂获取实体管理器
        EntityManager entityManager = factory.createEntityManager();

        //3.获取事务对象，开启事务
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        Poll poll = entityManager.find(Poll.class, 5);
        System.out.println("Title: "+ poll.getTitle());

        //5.提交事务（回滚事务）
        transaction.commit();
        //6.释放资源
        entityManager.close();
        factory.close();
    }

}
