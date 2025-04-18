package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.syemessenger.websocket.WebSocketServer;
import io.syemessenger.websocket.WebSocketServlet;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "io.syemessenger")
@EnableJpaRepositories
@EnableTransactionManagement
public class AppConfiguration {

  @Bean
  public WebSocketServer webSocketServer(ServiceConfig config, WebSocketServlet servlet) {
    return WebSocketServer.launch(config.port(), servlet);
  }

  @Bean
  public JsonMapper jsonMapper() {
    return JsonMappers.jsonMapper();
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setDatabase(Database.POSTGRESQL);

    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("io.syemessenger");
    factory.setDataSource(dataSource);
    return factory;
  }

  @Bean
  public DataSource dataSource(ServiceConfig config) {
    final var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.dbUrl());
    hikariConfig.setUsername(config.dbUser());
    hikariConfig.setPassword(config.dbPassword());
    hikariConfig.setConnectionInitSql("CREATE SCHEMA IF NOT EXISTS syemessenger");
    hikariConfig.setSchema("syemessenger");
    return new HikariDataSource(hikariConfig);
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager txManager = new JpaTransactionManager();
    txManager.setEntityManagerFactory(entityManagerFactory);
    return txManager;
  }
}
