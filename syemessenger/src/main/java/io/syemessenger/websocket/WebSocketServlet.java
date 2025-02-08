package io.syemessenger.websocket;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.ServiceRegistry;
import jakarta.inject.Named;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;

@Named
public class WebSocketServlet extends JettyWebSocketServlet {

  private final JsonMapper jsonMapper;
  private final ServiceRegistry serviceRegistry;

  public WebSocketServlet(JsonMapper jsonMapper, ServiceRegistry serviceRegistry) {
    this.jsonMapper = jsonMapper;
    this.serviceRegistry = serviceRegistry;
  }

  @Override
  public void configure(JettyWebSocketServletFactory factory) {
    factory.addMapping("/", (req, res) -> new WebSocketHandler(jsonMapper, serviceRegistry));
  }
}
