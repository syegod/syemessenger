package io.syemessenger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SampleTest {

  @BeforeAll
  static void beforeAll() {}

  @AfterAll
  static void afterAll() {}

  @Test
  void testHello() {
    System.out.println("test");
  }
}
