package io.syemessenger.api;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;

public class CreateTestcontainer {

  public static void main(String[] args) throws InterruptedException {
    PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:11.10");
    postgres.start();
    var username = postgres.getUsername();
    var password = postgres.getPassword();
    var jdbcUrl = postgres.getJdbcUrl();
    // perform db operations
    Thread.sleep(Long.MAX_VALUE);
    postgres.stop();
  }
}
