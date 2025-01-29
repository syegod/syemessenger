package io.syemessenger.websocket;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.RoomService;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.AccountService;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.api.account.UpdateAccountRequest;
import io.syemessenger.api.room.BlockMembersRequest;
import io.syemessenger.api.room.CreateRoomRequest;
import io.syemessenger.api.room.ListRoomsRequest;
import io.syemessenger.api.room.RemoveMembersRequest;
import io.syemessenger.api.room.UnblockMembersRequest;
import io.syemessenger.api.room.UpdateRoomRequest;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
public class WebSocketHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);

  private final JsonMapper jsonMapper;
  private final MessageCodec messageCodec;
  private final AccountService accountService;
  private final RoomService roomService;

  private SessionContext sessionContext;

  public WebSocketHandler(
      JsonMapper jsonMapper,
      MessageCodec messageCodec,
      AccountService accountService,
      RoomService roomService) {
    this.jsonMapper = jsonMapper;
    this.accountService = accountService;
    this.messageCodec = messageCodec;
    this.roomService = roomService;
  }

  @OnWebSocketClose
  public void onWebSocketClose(int statusCode, String reason) {
    sessionContext = null;
    LOGGER.info("WebSocket Close: {} - {}", statusCode, reason);
  }

  @OnWebSocketOpen
  public void onWebSocketOpen(Session session) {
    sessionContext = new SessionContext(session, jsonMapper);
    LOGGER.info("WebSocket Open: {}", session);
  }

  @OnWebSocketError
  public void onWebSocketError(Throwable cause) {
    LOGGER.warn("WebSocket Error", cause);
  }

  @OnWebSocketMessage
  public void onWebSocketText(String message) {
    LOGGER.info("Received message [{}]", message);
    try {
      final var serviceMessage = jsonMapper.readValue(message, ServiceMessage.class);
      final var qualifier = serviceMessage.qualifier();

      if (qualifier == null) {
        throw new RuntimeException("Wrong message: qualifier is missing");
      }

      final var request = messageCodec.decode(serviceMessage);

      switch (qualifier) {
        case "createAccount":
          accountService.createAccount(sessionContext, (CreateAccountRequest) request);
          break;
        case "updateAccount":
          accountService.updateAccount(sessionContext, (UpdateAccountRequest) request);
          break;
        case "showAccount":
          accountService.showAccount(sessionContext, (Long) request);
          break;
        case "login":
          accountService.login(sessionContext, (LoginAccountRequest) request);
          break;
        case "getSessionAccount":
          accountService.getSessionAccount(sessionContext);
          break;
        case "createRoom":
          roomService.createRoom(sessionContext, (CreateRoomRequest) request);
          break;
        case "updateRoom":
          roomService.updateRoom(sessionContext, (UpdateRoomRequest) request);
          break;
        case "getRoom":
          roomService.getRoom(sessionContext, (Long) request);
          break;
        case "joinRoom":
          roomService.joinRoom(sessionContext, (String) request);
          break;
        case "leaveRoom":
          roomService.leaveRoom(sessionContext, (Long) request);
          break;
        case "removeRoomMembers":
          roomService.removeRoomMembers(sessionContext, (RemoveMembersRequest) request);
          break;
        case "blockRoomMembers":
          roomService.blockRoomMembers(sessionContext, (BlockMembersRequest) request);
          break;
        case "unblockRoomMembers":
          roomService.unblockRoomMembers(sessionContext, (UnblockMembersRequest) request);
          break;
        case "listRooms":
          roomService.listRooms(sessionContext, (ListRoomsRequest) request);
          break;
        default:
          throw new IllegalArgumentException("Wrong request: " + request);
      }
    } catch (Exception e) {
      LOGGER.error("[onWebSocketText] Failed to parse message [{}]", message, e);
      throw new RuntimeException(e);
    }
  }
}
