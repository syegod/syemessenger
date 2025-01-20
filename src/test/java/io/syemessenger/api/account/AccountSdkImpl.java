package io.syemessenger.api.account;

import io.syemessenger.api.ServiceMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public class AccountSdkImpl implements AccountSdk {

  private WebSocket ws;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final BlockingQueue<ServiceMessage> messageQueue = new ArrayBlockingQueue<>(64);

  public AccountSdkImpl() {
    HttpClient client = HttpClient.newHttpClient();
    WebSocket.Builder builder = client.newWebSocketBuilder();

    ws = builder.buildAsync(URI.create("ws://localhost:8080/"), new WebSocketListener()).join();
  }

  @Override
  public Long createAccount(CreateAccountRequest request) {
    try {
      final var message = new ServiceMessage();
      message.qualifier("createAccount");
      message.data(request);
      ws.sendText(objectMapper.writeValueAsString(message), true).join();
      final var response = messageQueue.poll(3, TimeUnit.SECONDS);
      if (response == null) {
        throw new RuntimeException("Poll failed");
      }
      return (Long) response.data();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public AccountInfo updateAccount(UpdateAccountRequest request) {
    return null;
  }

  @Override
  public void login(LoginAccountRequest request) {}

  @Override
  public AccountInfo getSessionAccount() {
    return null;
  }

  @Override
  public PublicAccountInfo showAccount(Long id) {
    try {
      final var message = new ServiceMessage();
      message.qualifier("showAccount");
      message.data(id);
      ws.sendText(objectMapper.writeValueAsString(message), true);
      final var response = messageQueue.poll(3, TimeUnit.SECONDS);
      if (response == null) {
        throw new RuntimeException("Poll failed");
      }
      return (PublicAccountInfo) response.data();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    ws.sendClose(WebSocket.NORMAL_CLOSURE, "Exit")
        .thenRun(() -> System.out.println("WebSocket closed"));
  }

  private class WebSocketListener implements WebSocket.Listener {

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      try {
        final var message = objectMapper.readValue(data.toString(), ServiceMessage.class);
        if (!messageQueue.offer(message, 3, TimeUnit.SECONDS)) {
          throw new RuntimeException("Offer failed");
        }
        return Listener.super.onText(webSocket, data, last);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
