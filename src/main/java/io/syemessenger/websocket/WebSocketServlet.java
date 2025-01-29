package io.syemessenger.websocket;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.account.AccountService;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.api.account.UpdateAccountRequest;
import jakarta.inject.Named;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;

@Named
public class WebSocketServlet extends JettyWebSocketServlet {

  private final JsonMapper jsonMapper;
  private final MessageCodec messageCodec;
  private final AccountService accountService;

  public WebSocketServlet(JsonMapper jsonMapper, AccountService accountService) {
    this.jsonMapper = jsonMapper;
    this.accountService = accountService;
    this.messageCodec =
        new MessageCodec(
            jsonMapper,
            map -> {
              map.put("createAccount", CreateAccountRequest.class);
              map.put("updateAccount", UpdateAccountRequest.class);
              map.put("showAccount", Long.class);
              map.put("login", LoginAccountRequest.class);
              map.put("getSessionAccount", Void.class);
            });
  }

  @Override
  public void configure(JettyWebSocketServletFactory factory) {
    factory.addMapping(
        "/", (req, res) -> new WebSocketHandler(jsonMapper, messageCodec, accountService));
  }
}
