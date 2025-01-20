package io.syemessenger;

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

  private Session session;

  @OnWebSocketClose
  public void onWebSocketClose(int statusCode, String reason) {
    this.session = null;
    LOGGER.info("WebSocket Close: {} - {}", statusCode, reason);
  }

  @OnWebSocketOpen
  public void onWebSocketOpen(Session session) {
    this.session = session;
    LOGGER.info("WebSocket Open: {}", session);
  }

  @OnWebSocketError
  public void onWebSocketError(Throwable cause) {
    LOGGER.warn("WebSocket Error", cause);
  }

  @OnWebSocketMessage
  public void onWebSocketText(String message) {
    LOGGER.info("Echoing back text message [{}]", message);
//    this.session.sendText(message, Callback.NOOP);
  }
}
