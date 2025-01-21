package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.account.AccountService;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;

public class WebSocketServlet extends JettyWebSocketServlet {

  private final JsonMapper jsonMapper;
  private final MessageCodec messageCodec;
  private final AccountService accountService;

  public WebSocketServlet(
      JsonMapper jsonMapper, MessageCodec messageCodec, AccountService accountService) {
    this.jsonMapper = jsonMapper;
    this.messageCodec = messageCodec;
    this.accountService = accountService;
  }

  @Override
  public void configure(JettyWebSocketServletFactory factory) {
    factory.addMapping(
        "/", (req, res) -> new WebSocketHandler(jsonMapper, messageCodec, accountService));
  }
}
