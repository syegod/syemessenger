package io.syemessenger;

import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.websocket.SessionContext;
import io.syemessenger.websocket.WebSocketHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvocationHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(InvocationHandler.class);

  private final Method method;
  private final Object target;
  private final MessageCodec messageCodec;

  public InvocationHandler(Method method, Object target, MessageCodec messageCodec) {
    this.method = method;
    this.target = target;
    this.messageCodec = messageCodec;
  }

  public void invoke(SessionContext sessionContext, ServiceMessage message) {
    try {
      method.invoke(target, sessionContext, message.data(messageCodec.decode(message)));
    } catch (InvocationTargetException e) {
      final var cause = e.getCause();
      LOGGER.error("Exception occurred", cause);
      if (cause instanceof ServiceException ex) {
        sessionContext.sendError(message.cid(), ex.errorCode(), ex.getMessage());
      } else {
        sessionContext.sendError(message.cid(), 500, "Internal service error: " + cause);
      }
    } catch (Exception ex) {
      LOGGER.error("Exception occurred", ex);
      sessionContext.sendError(message.cid(), 500, "Internal service error: " + ex);
    }
  }
}
