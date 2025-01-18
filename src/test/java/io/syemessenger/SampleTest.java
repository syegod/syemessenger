package io.syemessenger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;


public class SampleTest {

  private static ConfigurableApplicationContext applicationContext;

  @BeforeAll
  static void beforeAll() {
    applicationContext = SpringApplication.run(WebSocketApplication.class);
  }

  @AfterAll
  static void afterAll() {
    if (applicationContext != null) {
      applicationContext.close();
    }
  }

  @Test
  void testHello() {
    System.out.println("test");
  }
}
