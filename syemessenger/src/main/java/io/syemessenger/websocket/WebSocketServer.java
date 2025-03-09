package io.syemessenger.websocket;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServer implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

  private final Server server;

  private WebSocketServer(Server server) {
    this.server = server;
  }

  public static WebSocketServer launch(int port, WebSocketServlet servlet) {
    Server server = new Server(port);

    try {
      ServletContextHandler contextHandler =
          new ServletContextHandler(ServletContextHandler.SESSIONS);
      contextHandler.setContextPath("/");
      server.setHandler(contextHandler);

      // Add websocket servlet
      JettyWebSocketServletContainerInitializer.configure(contextHandler, null);
      contextHandler.addServlet(new ServletHolder("ws", servlet), "/");

      server.start();

      LOGGER.info("WebSocket server started");
    } catch (Exception ex) {
      LOGGER.error("Exception occurred on server start", ex);
      try {
        server.stop();
      } catch (Exception e) {
        LOGGER.error("Exception occurred on server stop", e);
      }
      throw new RuntimeException(ex);
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
