package io.syemessenger.environment;

import io.syemessenger.ServiceBootstrap;
import io.syemessenger.ServiceConfig;
import org.testcontainers.containers.PostgreSQLContainer;

public class IntegrationEnvironment {

  private PostgreSQLContainer postgres;
  private ServiceBootstrap serviceBootstrap;

  public void start() {
    try {
      postgres = new PostgreSQLContainer("postgres:16-alpine");
      postgres.start();

      serviceBootstrap = new ServiceBootstrap(new ServiceConfig());
      serviceBootstrap.start();
    } catch (Exception e) {
      close();
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
