package io.syemessenger.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.JsonMappers;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.room.RoomSdk;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSdk implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientSdk.class);

  private final MessageCodec messageCodec = ClientCodec.getInstance();

  private final WebSocket ws;
  private final JsonMapper objectMapper = JsonMappers.jsonMapper();
  private final BlockingQueue<ServiceMessage> messageQueue = new ArrayBlockingQueue<>(64);
  private final CountDownLatch latch = new CountDownLatch(1);

  public ClientSdk() {
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
  public void close() {
    ws.sendClose(WebSocket.NORMAL_CLOSURE, "Exit")
        .thenRun(() -> System.out.println("WebSocket closed"));
  }

  private Object pollResponse(UUID cid) {
    final ServiceMessage response;
    try {
      response = messageQueue.poll(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (response == null) {
      throw new RuntimeException("Poll failed");
    }

    if (!response.cid().equals(cid)) {
      throw new RuntimeException(
          "Invalid cid: request = " + cid + ", response = " + response.cid());
    }

    final var data = messageCodec.decode(response);

    if (data instanceof ErrorData errorData) {
      throw new ServiceException(errorData.errorCode(), errorData.errorMessage());
    }

    return data;
  }

  private void sendText(ServiceMessage message) {
    try {
      ws.sendText(objectMapper.writeValueAsString(message), true);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T api(Class<T> api) {
    if (!api.isInterface()) {
      throw new IllegalArgumentException("Must be interface: " + api);
    }
    //noinspection unchecked
    return (T)
        Proxy.newProxyInstance(
            api.getClassLoader(),
            new Class[] {api},
            (proxy, method, args) -> {
              Object check = toStringOrEqualsOrHashCode(method.getName(), api, args);
              if (check != null) {
                return check;
              }
              final var request = args != null ? args[0] : null;
              final var name = method.getName();
              final var cid = UUID.randomUUID();
              sendText(new ServiceMessage().cid(cid).qualifier(name).data(request));
              return pollResponse(cid);
            });
  }

  public AccountSdk accountSdk() {
    return api(AccountSdk.class);
  }

  public RoomSdk roomSdk() {
    return api(RoomSdk.class);
  }

  private Object toStringOrEqualsOrHashCode(String method, Class<?> api, Object... args) {
    return switch (method) {
      case "toString" -> api.toString();
      case "equals" -> api.equals(args[0]);
      case "hashCode" -> api.hashCode();
      default -> null;
    };
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
