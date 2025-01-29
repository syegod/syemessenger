package io.syemessenger.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.ErrorData;
import io.syemessenger.api.ServiceMessage;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

public class SessionContext {

  private final Session session;
  private final JsonMapper jsonMapper;

  private Long accountId;

  public SessionContext(Session session, JsonMapper jsonMapper) {
    this.session = session;
    this.jsonMapper = jsonMapper;
  }

  public void send(ServiceMessage message) {
    try {
      session.sendText(jsonMapper.writeValueAsString(message), Callback.NOOP);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public void sendError(int errorCode, String errorMessage) {
    send(new ServiceMessage().qualifier("error").data(new ErrorData(errorCode, errorMessage)));
  }

  public Long accountId() {
    return accountId;
  }

  public SessionContext accountId(Long accountId) {
    this.accountId = accountId;
    return this;
  }

  public boolean isLoggedIn() {
    return accountId != null;
  }
}
