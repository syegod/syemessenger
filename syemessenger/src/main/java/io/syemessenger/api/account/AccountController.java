package io.syemessenger.api.account;

import io.syemessenger.annotations.RequestController;
import io.syemessenger.annotations.RequestHandler;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.util.regex.Pattern;

@Named
@RequestController
public class AccountController {

  private final AccountService accountService;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @RequestHandler("v1/syemessenger/createAccount")
  public void createAccount(SessionContext sessionContext, CreateAccountRequest request) {
    final var username = request.username();
    if (username == null) {
      throw new ServiceException(400, "Missing or invalid: username");
    }
    if (username.length() < 8 || username.length() > 64) {
      throw new ServiceException(400, "Missing or invalid: username");
    }

    final var email = request.email();
    if (email == null) {
      throw new ServiceException(400, "Missing or invalid: email");
    }
    if (email.length() < 8 || email.length() > 64) {
      throw new ServiceException(400, "Missing or invalid: email");
    }

    if (!isEmailValid(email)) {
      throw new ServiceException(400, "Missing or invalid: email");
    }

    final var password = request.password();
    if (password == null) {
      throw new ServiceException(400, "Missing or invalid: password");
    }
    if (password.length() < 8 || password.length() > 64) {
      throw new ServiceException(400, "Missing or invalid: password");
    }

    final var accountInfo = accountService.createAccount(request);

    sessionContext.send(new ServiceMessage().qualifier("createAccount").data(accountInfo));
  }

  @RequestHandler("v1/syemessenger/updateAccount")
  public void updateAccount(SessionContext sessionContext, UpdateAccountRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var username = request.username();
    if (username != null) {
      if (username.length() < 8 || username.length() > 64) {
        throw new ServiceException(400, "Invalid: username");
      }
    }

    final var email = request.email();
    if (email != null) {
      if (email.length() < 8 || email.length() > 64) {
        throw new ServiceException(400, "Invalid: email");
      }
      if (!isEmailValid(email)) {
        throw new ServiceException(400, "Invalid: email");
      }
    }

    final var password = request.password();
    if (password != null) {
      if (password.length() < 8 || password.length() > 64) {
        throw new ServiceException(400, "Invalid: password");
      }
    }

    final var accountInfo = accountService.updateAccount(request, sessionContext.accountId());

    sessionContext.send(new ServiceMessage().qualifier("updateAccount").data(accountInfo));
  }

  @RequestHandler("v1/syemessenger/login")
  public void login(SessionContext sessionContext, LoginAccountRequest request) {
    final var username = request.username();
    final var email = request.email();
    if (username != null && email != null) {
      throw new ServiceException(401, "Login failed");
    }
    if (username == null && email == null) {
      throw new ServiceException(401, "Login failed");
    }

    final var password = request.password();
    if (password == null) {
      throw new ServiceException(401, "Login failed");
    }

    final var id = accountService.login(request);

    sessionContext.accountId(id);

    sessionContext.send(new ServiceMessage().qualifier("login").data(id));
  }

  @RequestHandler("v1/syemessenger/getAccount")
  public void getAccount(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var account = accountService.getAccount(id);

    sessionContext.send(new ServiceMessage().qualifier("getAccount").data(account));
  }

  @RequestHandler("v1/syemessenger/getRooms")
  public void getRooms(SessionContext sessionContext, GetRoomsRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var offset = request.offset();
    if (offset != null && offset < 0) {
      throw new ServiceException(400, "Missing or invalid: offset");
    }

    final var limit = request.limit();
    if (limit != null && (limit < 0 || limit > 50)) {
      throw new ServiceException(400, "Missing or invalid: limit");
    }

    final var response = accountService.getRooms(sessionContext.accountId(), request);

    sessionContext.send(new ServiceMessage().qualifier("getRooms").data(response));
  }

  private static boolean isEmailValid(String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }
}
