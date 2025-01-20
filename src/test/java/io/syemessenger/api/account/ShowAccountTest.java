package io.syemessenger.api.account;

import io.syemessenger.WebSocketServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ShowAccountTest {

  private static WebSocketServer server;

  @BeforeAll
  static void beforeAll() {
    server = WebSocketServer.start(8080);
  }

  @AfterAll
  static void afterAll() {
    if (server != null) {
      server.close();
    }
  }

  @Test
  void testShowAccountId() {

  }
}
