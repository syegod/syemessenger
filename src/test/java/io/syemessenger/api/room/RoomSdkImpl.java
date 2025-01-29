package io.syemessenger.api.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.JsonMappers;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.ErrorData;
import io.syemessenger.api.ServiceException;
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

public class RoomSdkImpl implements RoomSdk {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomSdkImpl.class);

  private final MessageCodec messageCodec;

  private final WebSocket ws;
  private final JsonMapper objectMapper = JsonMappers.jsonMapper();
  private final BlockingQueue<ServiceMessage> messageQueue = new ArrayBlockingQueue<>(64);
  private final CountDownLatch latch = new CountDownLatch(1);

  public RoomSdkImpl(MessageCodec messageCodec) {
    this.messageCodec = messageCodec;
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
  public RoomInfo createRoom(CreateRoomRequest request) {
    final var message = new ServiceMessage().qualifier("createRoom").data(request);
    sendText(message);
    return (RoomInfo) pollResponse();
  }

  @Override
  public RoomInfo updateRoom(UpdateRoomRequest request) {
    final var message = new ServiceMessage().qualifier("updateRoom").data(request);
    sendText(message);
    return (RoomInfo) pollResponse();
  }

  @Override
  public RoomInfo getRoom(Long id) {
    final var message = new ServiceMessage().qualifier("getRoom").data(id);
    sendText(message);
    return (RoomInfo) pollResponse();
  }

  @Override
  public Long joinRoom(String name) {
    final var message = new ServiceMessage().qualifier("joinRoom").data(name);
    sendText(message);
    return (Long) pollResponse();
  }

  @Override
  public Long leaveRoom(Long id) {
    final var message = new ServiceMessage().qualifier("leaveRoom").data(id);
    sendText(message);
    return (Long) pollResponse();
  }

  @Override
  public Long removeMembers(RemoveMembersRequest request) {
    final var message = new ServiceMessage().qualifier("removeMembers").data(request);
    sendText(message);
    return (Long) pollResponse();
  }

  @Override
  public Long blockMembers(BlockMembersRequest request) {
    final var message = new ServiceMessage().qualifier("blockMembers").data(request);
    sendText(message);
    return (Long) pollResponse();
  }

  @Override
  public Long unblockMembers(UnblockMembersRequest request) {
    final var message = new ServiceMessage().qualifier("unblockMembers").data(request);
    sendText(message);
    return (Long) pollResponse();
  }

  @Override
  public RoomInfoList listRooms(ListRoomsRequest request) {
    final var message = new ServiceMessage().qualifier("unblockMembers").data(request);
    sendText(message);
    return (RoomInfoList) pollResponse();
  }

  @Override
  public void close() {
    ws.sendClose(WebSocket.NORMAL_CLOSURE, "Exit")
        .thenRun(() -> System.out.println("WebSocket closed"));
  }

  private Object pollResponse() {
    final ServiceMessage response;
    try {
      response = messageQueue.poll(3, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (response == null) {
      throw new RuntimeException("Poll failed");
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
