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
public class EchoWebSocket {
  private static final Logger LOG = LoggerFactory.getLogger(EchoWebSocket.class);
  private Session session;

  @OnWebSocketClose
  public void onWebSocketClose(int statusCode, String reason) {
    this.session = null;
    LOG.info("WebSocket Close: {} - {}", statusCode, reason);
  }

  @OnWebSocketOpen
  public void onWebSocketOpen(Session session) {
    this.session = session;
    LOG.info("WebSocket Open: {}", session);
    this.session.sendText("You are now connected to " + this.getClass().getName(), Callback.NOOP);
  }

  @OnWebSocketError
  public void onWebSocketError(Throwable cause) {
    LOG.warn("WebSocket Error", cause);
  }

  @OnWebSocketMessage
  public void onWebSocketText(String message) {
    LOG.info("Echoing back text message [{}]", message);
    this.session.sendText(message, Callback.NOOP);
  }
}
