package io.syemessenger.api.account;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.JsonMappers;
import io.syemessenger.api.ServiceMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountSdkImpl implements AccountSdk {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccountSdkImpl.class);

  private WebSocket ws;
  private final JsonMapper objectMapper = JsonMappers.jsonMapper();
  private final BlockingQueue<ServiceMessage> messageQueue = new ArrayBlockingQueue<>(64);
  private final CountDownLatch latch = new CountDownLatch(1);

  public AccountSdkImpl() {
    HttpClient client = HttpClient.newHttpClient();
    WebSocket.Builder builder = client.newWebSocketBuilder();

    ws = builder.buildAsync(URI.create("ws://localhost:8080/"), new WebSocketListener()).join();
    try {
      if (!latch.await(3, TimeUnit.SECONDS)) {
        throw new RuntimeException("Cannot establish connection");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Long createAccount(CreateAccountRequest request) {
    try {
      final var message = new ServiceMessage();
      message.qualifier("createAccount");
      message.data(request);
      ws.sendText(objectMapper.writeValueAsString(message), true);
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
  public Long login(LoginAccountRequest request) {
    return null;
  }

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
    public void onOpen(WebSocket webSocket) {
      LOGGER.debug("WebSocket connection opened {}", webSocket);
      latch.countDown();
      Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      try {
        final var message = objectMapper.readValue(data.toString(), ServiceMessage.class);
        if (!messageQueue.offer(message, 3, TimeUnit.SECONDS)) {
          throw new RuntimeException("Offer failed");
        }
        return Listener.super.onText(webSocket, data, last);
      } catch (Exception e) {
        LOGGER.error("[onText] Exception occurred", e);
        throw new RuntimeException(e);
      }
    }
  }
}
