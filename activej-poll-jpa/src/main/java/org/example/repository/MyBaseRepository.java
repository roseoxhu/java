package org.example.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface MyBaseRepository<T, ID> extends Repository<T, ID> {
    // @Query 中可写 HQL 和 SQL，如果是 SQL，则 nativeQuery = true
    // Caused by: org.hibernate.hql.internal.ast.QuerySyntaxException: DUAL is not mapped [SELECT now() FROM DUAL]
    //@Query("SELECT now() FROM DUAL")
    @Query(value = "SELECT now() FROM DUAL", nativeQuery = true)
    @NotNull String now();
}
