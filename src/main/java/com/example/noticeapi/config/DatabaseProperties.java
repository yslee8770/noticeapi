package com.example.noticeapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@Setter
public class DatabaseProperties {
  private DataSourceProperties master = new DataSourceProperties();
  private DataSourceProperties slave = new DataSourceProperties();

  @Getter
  @Setter
  public static class DataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
  }
}
