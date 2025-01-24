package io.syemessenger.api.account;

import io.syemessenger.websocket.SenderContext;
import jakarta.inject.Named;

@Named
public class AccountService {

  public void createAccount(SenderContext senderContext, CreateAccountRequest request) {
    final var username = request.username();
    if (username == null) {
      senderContext.sendError(400, "Missing or invalid: username");
      return;
    }
    if (username.length() < 6 || username.length() > 30) {
      senderContext.sendError(400, "Missing or invalid: username");
      return;
    }

    final var email = request.email();
    if (email == null) {
      senderContext.sendError(400, "Missing or invalid: email");
      return;
    }
    if (email.length() < 10 || email.length() > 50) {
      senderContext.sendError(400, "Missing or invalid: email");
      return;
    }

    final var password = request.password();
    if (password == null) {
      senderContext.sendError(400, "Missing or invalid: password");
      return;
    }
    if (password.length() < 6 || password.length() > 25) {
      senderContext.sendError(400, "Missing or invalid: password");
      return;
    }
  }

  public void updateAccount(SenderContext senderContext, UpdateAccountRequest request) {}

  public void login(SenderContext senderContext, LoginAccountRequest request) {
  }

  public void getSessionAccount(SenderContext senderContext) {
  }

  public void showAccount(SenderContext senderContext, Long id) {
  }
}
