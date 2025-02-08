package io.syemessenger.websocket;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.ServiceRegistry;
import io.syemessenger.api.ServiceMessage;
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
  private final ServiceRegistry serviceRegistry;

  private SessionContext sessionContext;

  public WebSocketHandler(JsonMapper jsonMapper, ServiceRegistry serviceRegistry) {
    this.jsonMapper = jsonMapper;
    this.serviceRegistry = serviceRegistry;
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

      final var invocationHandler = serviceRegistry.lookup(qualifier);
      if (invocationHandler == null) {
        throw new RuntimeException("Wrong message: request handler is missing");
      }

      invocationHandler.invoke(sessionContext, serviceMessage);
    } catch (Exception e) {
      LOGGER.error("[onWebSocketText] Exception onMessage [{}]", message, e);
      throw new RuntimeException(e);
    }
  }
}
