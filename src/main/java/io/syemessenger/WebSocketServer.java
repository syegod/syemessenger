package io.syemessenger;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServer implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

  private final Server server;

  public WebSocketServer(Server server) {
    this.server = server;
  }

  public static WebSocketServer start(int port) {
    Server server = new Server(port);

    try {
      ServletContextHandler contextHandler =
          new ServletContextHandler(ServletContextHandler.SESSIONS);
      contextHandler.setContextPath("/");
      server.setHandler(contextHandler);

      // Add websocket servlet
      JettyWebSocketServletContainerInitializer.configure(contextHandler, null);
      contextHandler.addServlet(new ServletHolder("echo", new EchoWebSocketServlet()), "/echo");

      server.start();
    } catch (Exception ex) {
      try {
        server.stop();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return new WebSocketServer(server);
  }

  @Override
  public void close() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
