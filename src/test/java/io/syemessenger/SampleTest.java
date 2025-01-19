package io.syemessenger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SampleTest {

  private static WebSocketServer server;

  @BeforeAll
  static void beforeAll() {
    server = WebSocketServer.start(8080);
  }

  @AfterAll
  static void afterAll() throws Exception {
    if (server != null) {
      server.close();
    }
  }

  @Test
  void testHello() {
    HttpClient client = HttpClient.newHttpClient();
    WebSocket.Builder builder = client.newWebSocketBuilder();

    WebSocket webSocket =
        builder.buildAsync(URI.create("ws://localhost:8080/echo"), new WebSocketListener()).join();

    webSocket.sendText("Hello, WebSocket!", true);

    try {
      Thread.sleep(10000); // Keep the WebSocket connection open for 10 seconds
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Close the WebSocket
    webSocket
        .sendClose(WebSocket.NORMAL_CLOSURE, "Bye")
        .thenRun(() -> System.out.println("WebSocket closed"));
  }

  static class WebSocketListener implements WebSocket.Listener {
    @Override
    public void onOpen(WebSocket webSocket) {
      System.out.println("WebSocket opened");
      webSocket.request(1); // Request 1 message to be received
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      System.out.println("Received message: " + data);
      webSocket.request(1); // Request next message
      return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      System.out.println("WebSocket closed with status: " + statusCode + ", reason: " + reason);
      return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
      System.err.println("Error occurred: " + error.getMessage());
    }
  }
}
