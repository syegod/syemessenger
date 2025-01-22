package io.syemessenger.api.account;

import io.syemessenger.SenderContext;
import io.syemessenger.api.ErrorData;
import io.syemessenger.api.ServiceMessage;
import jakarta.inject.Named;

@Named
public class AccountService {

  public void createAccount(SenderContext senderContext, CreateAccountRequest request) {
    final var username = request.username();
    if (username == null) {
      senderContext.send(new ServiceMessage().qualifier("createAccount").data(new ErrorData(400, "Missing or invalid: username")));
      return;
    }
    if (username.length() < 6 || username.length() > 30) {
      senderContext.send(new ServiceMessage().qualifier("createAccount").data(new ErrorData(400, "Missing or invalid: username")));
    }

    final var email = request.email();
    if (email == null){
      senderContext.send(new ServiceMessage().qualifier("createAccount").data(new ErrorData(400, "Missing or invalid: email")));
      return;
    }
    if (email.length() < 10 || email.length() > 50) {
      senderContext.send(new ServiceMessage().qualifier("createAccount").data(new ErrorData(400, "Missing or invalid: email")));
    }

    final var password = request.password();
    if (password == null){
      senderContext.send(new ServiceMessage().qualifier("createAccount").data(new ErrorData(400, "Missing or invalid: password")));
      return;
    }
    if (password.length() < 6 || password.length() > 25) {
      senderContext.send(new ServiceMessage().qualifier("createAccount").data(new ErrorData(400, "Missing or invalid: password")));
    }



  }

  public void updateAccount(SenderContext senderContext, UpdateAccountRequest request) {
  }

  public void login(SenderContext senderContext, LoginAccountRequest request) {
    return;
  }

  public void getSessionAccount(SenderContext senderContext) {
    return;
  }

  public void showAccount(SenderContext senderContext, Long id) {
    return;
  }
}
