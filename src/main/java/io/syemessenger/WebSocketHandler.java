package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.AccountSession;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.AccountService;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.UpdateAccountRequest;
import org.eclipse.jetty.websocket.api.Callback;
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

  private AccountSession accountSession;

  public WebSocketHandler(JsonMapper jsonMapper, AccountService accountService) {
    this.jsonMapper = jsonMapper;
    this.accountService = accountService;
  }

  @OnWebSocketClose
  public void onWebSocketClose(int statusCode, String reason) {
    this.accountSession = null;
    LOGGER.info("WebSocket Close: {} - {}", statusCode, reason);
  }

  @OnWebSocketOpen
  public void onWebSocketOpen(Session session) {
    accountSession = new AccountSession(session, null);
    LOGGER.info("WebSocket Open: {}", session);
  }

  @OnWebSocketError
  public void onWebSocketError(Throwable cause) {
    LOGGER.warn("WebSocket Error", cause);
  }

  @OnWebSocketMessage
  public void onWebSocketText(String message) {
    LOGGER.info("Received message [{}]", message);
    //    this.session.sendText(message, Callback.NOOP);
    try {
      final var serviceMessage = jsonMapper.readValue(message, ServiceMessage.class);
      final var qualifier = serviceMessage.qualifier();
      final var data = serviceMessage.data();

      if (qualifier == null) {
        throw new RuntimeException("Wrong message: qualifier is missing");
      }

      if (qualifier.equals("createAccount")) {
        final var createAccountRequest = jsonMapper.convertValue(data, CreateAccountRequest.class);
        final var accountId = accountService.createAccount(createAccountRequest);

        accountSession.session().sendText(
            jsonMapper.writeValueAsString(
                new ServiceMessage().qualifier(qualifier).data(accountId)),
            Callback.NOOP);
      } else if (qualifier.equals("updateAccount")) {
        final var updateAccountRequest = jsonMapper.convertValue(data, UpdateAccountRequest.class);
        final var accountInfo = accountService.updateAccount(accountSession, updateAccountRequest);

        accountSession.session().sendText(
            jsonMapper.writeValueAsString(
                new ServiceMessage().qualifier(qualifier).data(accountInfo)),
            Callback.NOOP);
      } else if (qualifier.equals("showAccount")) {
        final var accountId = jsonMapper.convertValue(data, Long.class);
        final var publicAccountInfo = accountService.showAccount(accountSession, accountId);

        accountSession.session().sendText(
            jsonMapper.writeValueAsString(
                new ServiceMessage().qualifier(qualifier).data(publicAccountInfo)),
            Callback.NOOP);
      }
    } catch (Exception e) {
      LOGGER.error("[onWebSocketText] Failed to parse message [{}]", message, e);
      throw new RuntimeException(e);
    }
  }
}
