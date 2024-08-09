package com.example.noticeapi.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseConfig {

  @Autowired
  private DatabaseProperties databaseProperties;

  @Bean
  public DataSource dataSource() {
    ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();

    DataSource masterDataSource = createDataSource(databaseProperties.getMaster());
    DataSource slaveDataSource = createDataSource(databaseProperties.getSlave());

    Map<Object, Object> dataSourceMap = new HashMap<>();
    dataSourceMap.put("master", masterDataSource);
    dataSourceMap.put("slave", slaveDataSource);

    routingDataSource.setTargetDataSources(dataSourceMap);
    routingDataSource.setDefaultTargetDataSource(masterDataSource);

    return routingDataSource;
  }

  @Bean
  public PlatformTransactionManager transactionManager(DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  private DataSource createDataSource(DatabaseProperties.DataSourceProperties properties) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(properties.getUrl());
    dataSource.setUsername(properties.getUsername());
    dataSource.setPassword(properties.getPassword());
    dataSource.setDriverClassName(properties.getDriverClassName());
    return dataSource;
  }
}
