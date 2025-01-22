package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.AccountService;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.api.account.UpdateAccountRequest;
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
  private final AccountService accountService;
  private final MessageCodec messageCodec;

  private SenderContext senderContext;

  public WebSocketHandler(
      JsonMapper jsonMapper, MessageCodec messageCodec, AccountService accountService) {
    this.jsonMapper = jsonMapper;
    this.accountService = accountService;
    this.messageCodec = messageCodec;
  }

  @OnWebSocketClose
  public void onWebSocketClose(int statusCode, String reason) {
    senderContext = null;
    LOGGER.info("WebSocket Close: {} - {}", statusCode, reason);
  }

  @OnWebSocketOpen
  public void onWebSocketOpen(Session session) {
    senderContext = new SenderContext(session, jsonMapper);
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
          accountService.createAccount(senderContext, (CreateAccountRequest) request);
          break;
        case "updateAccount":
          accountService.updateAccount(senderContext, (UpdateAccountRequest) request);
          break;
        case "showAccount":
          accountService.showAccount(senderContext, (Long) request);
          break;
        case "login":
          accountService.login(senderContext, (LoginAccountRequest) request);
          break;
        case "getSessionAccount":
          accountService.getSessionAccount(senderContext);
          break;
      }
    } catch (Exception e) {
      LOGGER.error("[onWebSocketText] Failed to parse message [{}]", message, e);
      throw new RuntimeException(e);
    }
  }
}
