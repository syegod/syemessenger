package io.syemessenger;

import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;

public class WebSocketServlet extends JettyWebSocketServlet {

  @Override
  public void configure(JettyWebSocketServletFactory factory) {
//    factory.register(WebSocketHandler.class);
    factory.addMapping("/", (req, res) -> new WebSocketHandler());
  }
}
