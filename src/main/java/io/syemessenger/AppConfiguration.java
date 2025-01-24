package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.websocket.WebSocketServer;
import io.syemessenger.websocket.WebSocketServlet;
import java.sql.Connection;
import java.sql.DriverManager;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan(basePackages = "io.syemessenger")
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
  public Liquibase liquibase(ServiceConfig config) throws Exception {
    try (Connection connection =
        DriverManager.getConnection(config.dbUrl(), config.dbUser(), config.dbPassword())) {
      final var database =
          DatabaseFactory.getInstance()
              .findCorrectDatabaseImplementation(new JdbcConnection(connection));
      final var liquibase =
          new Liquibase("dbchangelog/dbchangelog.xml", new ClassLoaderResourceAccessor(), database);

      liquibase.update();

      return liquibase;
    }
  }
}
