package io.syemessenger.websocket;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.ServiceRegistry;
import io.syemessenger.SubscriptionRegistry;
import jakarta.inject.Named;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;

@Named
public class WebSocketServlet extends JettyWebSocketServlet {

  private final JsonMapper jsonMapper;
  private final ServiceRegistry serviceRegistry;
  private final SubscriptionRegistry subscriptionRegistry;

  public WebSocketServlet(
      JsonMapper jsonMapper,
      ServiceRegistry serviceRegistry,
      SubscriptionRegistry subscriptionRegistry) {
    this.jsonMapper = jsonMapper;
    this.serviceRegistry = serviceRegistry;
    this.subscriptionRegistry = subscriptionRegistry;
  }

  @Override
  public void configure(JettyWebSocketServletFactory factory) {
    factory.addMapping(
        "/", (req, res) -> new WebSocketHandler(jsonMapper, serviceRegistry, subscriptionRegistry));
  }
}
