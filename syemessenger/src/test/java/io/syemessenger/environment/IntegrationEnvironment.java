package io.syemessenger.environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.syemessenger.ServiceBootstrap;
import io.syemessenger.ServiceConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.testcontainers.containers.PostgreSQLContainer;

public class IntegrationEnvironment implements AutoCloseable {

  private PostgreSQLContainer postgres;
  private ServiceBootstrap serviceBootstrap;

  public void start() {
    try {
      postgres = new PostgreSQLContainer("postgres:16-alpine");
      postgres.withExposedPorts(5432);
      postgres.start();
      serviceBootstrap =
          new ServiceBootstrap(
              new ServiceConfig()
                  .dbUrl(postgres.getJdbcUrl())
                  .dbUser(postgres.getUsername())
                  .dbPassword(postgres.getPassword()));
      serviceBootstrap.start();

    } catch (Exception e) {
      close();
      throw new RuntimeException(e);
    }
  }

  public PostgreSQLContainer getPostgresContainer() {
    return postgres;
  }

  public void close() {
    if (postgres != null) {
      postgres.close();
    }
    if (serviceBootstrap != null) {
      serviceBootstrap.close();
    }
  }
}
