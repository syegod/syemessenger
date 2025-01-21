package io.syemessenger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.ServiceMessage;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;

public class SenderContext {

  private final Session session;
  private final JsonMapper jsonMapper;

  public SenderContext(Session session, JsonMapper jsonMapper) {
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
}
