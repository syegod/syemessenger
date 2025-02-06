package io.syemessenger.environment;

import io.syemessenger.ServiceBootstrap;
import io.syemessenger.ServiceConfig;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

  public <T> T getBean(Class<?> clazz) {
    //noinspection unchecked
    return (T) serviceBootstrap.applicationContext().getBean(clazz);
  }

  public static void cleanTables(DataSource dataSource) {
    try (final var connection = dataSource.getConnection()) {
      String truncateQuery =
          "TRUNCATE TABLE accounts CASCADE; "
              + "TRUNCATE TABLE rooms CASCADE; "
              + "TRUNCATE TABLE messages CASCADE;";

      try (PreparedStatement statement = connection.prepareStatement(truncateQuery)) {
        statement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
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
