package io.syemessenger;

import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.websocket.SessionContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InvocationHandler {

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
      method.invoke(target, sessionContext, messageCodec.decode(message));
    } catch (InvocationTargetException e) {
      final var th = e.getTargetException();
      if (th instanceof ServiceException ex) {
        sessionContext.sendError(ex.errorCode(), ex.getMessage());
      } else {
        sessionContext.sendError(500, "Internal service error: " + th);
      }
    } catch (Exception ex) {
      sessionContext.sendError(500, "Internal service error: " + ex);
    }
  }
}
