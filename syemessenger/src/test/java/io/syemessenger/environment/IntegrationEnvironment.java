package io.syemessenger.environment;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import io.syemessenger.ServiceBootstrap;
import io.syemessenger.ServiceConfig;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;

public class IntegrationEnvironment implements AutoCloseable {

  private PostgreSQLContainer postgres;
  private KafkaContainer kafka;
  private ServiceBootstrap serviceBootstrap;

  public void start() {
    try {
      postgres = new PostgreSQLContainer("postgres:16-alpine");
      postgres.withExposedPorts(5432);
      postgres.start();

      kafka = new KafkaContainer("apache/kafka-native:3.8.0");
      kafka.withExposedPorts(9092);
      kafka.start();

      serviceBootstrap =
          new ServiceBootstrap(
              new ServiceConfig()
                  .dbUrl(postgres.getJdbcUrl())
                  .dbUser(postgres.getUsername())
                  .dbPassword(postgres.getPassword())
                  .kafkaBootstrapServers(kafka.getBootstrapServers())
                  .kafkaConsumerGroup("messages-group0")
                  .roomOutboxProcessorRunDelay(300)
                  .shouldRunRoomOutboxProcessor(true));

      serviceBootstrap.start();
    } catch (Exception e) {
      close();
      throw new RuntimeException(e);
    }
  }

  public PostgreSQLContainer getPostgresContainer() {
    return postgres;
  }

  public <T> T getBean(Class<T> clazz) {
    return serviceBootstrap.applicationContext().getBean(clazz);
  }

  public static void cleanTables(DataSource dataSource) {
    try (final var connection = dataSource.getConnection()) {
      String truncateQuery =
          "TRUNCATE TABLE accounts RESTART IDENTITY CASCADE; "
              + "TRUNCATE TABLE rooms RESTART IDENTITY CASCADE; "
              + "TRUNCATE TABLE messages RESTART IDENTITY CASCADE;";

      try (PreparedStatement statement = connection.prepareStatement(truncateQuery)) {
        statement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    if (serviceBootstrap != null) {
      serviceBootstrap.close();
    }
    if (postgres != null) {
      postgres.close();
    }
    if (kafka != null) {
      kafka.close();
    }
  }
}
