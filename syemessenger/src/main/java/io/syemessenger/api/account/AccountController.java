package io.syemessenger.api.account;

import io.syemessenger.annotations.RequestController;
import io.syemessenger.annotations.RequestHandler;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.room.RoomMappers;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;

@Named
@RequestController
public class AccountController {

  private final AccountService accountService;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @RequestHandler(value = "v1/syemessenger/createAccount", requestType = CreateAccountRequest.class)
  public void createAccount(SessionContext sessionContext, ServiceMessage message) {
    final var request = (CreateAccountRequest) message.data();

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

    Account account;
    try {
      account = accountService.createAccount(request);
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot create account: already exists");
      }
      throw e;
    }
    sessionContext.send(message.clone().data(AccountMappers.toAccountInfo(account)));
  }

  @RequestHandler(value = "v1/syemessenger/updateAccount", requestType = UpdateAccountRequest.class)
  public void updateAccount(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (UpdateAccountRequest) message.data();

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

    Account account;
    try {
      account = accountService.updateAccount(request, sessionContext.accountId());
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot update account: already exists");
      }
      throw e;
    }

    sessionContext.send(message.clone().data(AccountMappers.toAccountInfo(account)));
  }

  @RequestHandler(value = "v1/syemessenger/login", requestType = LoginAccountRequest.class)
  public void login(SessionContext sessionContext, ServiceMessage message) {
    final var request = (LoginAccountRequest) message.data();

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

    sessionContext.send(message.clone().data(id));
  }

  @RequestHandler(value = "v1/syemessenger/getAccount", requestType = Long.class)
  public void getAccount(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var id = (Long) message.data();

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var account = accountService.getAccount(id);

    sessionContext.send(message.clone().data(AccountMappers.toAccountInfo(account)));
  }

  @RequestHandler(value = "v1/syemessenger/getRooms", requestType = GetRoomsRequest.class)
  public void getRooms(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (GetRoomsRequest) message.data();

    final var offset = request.offset();
    if (offset != null && offset < 0) {
      throw new ServiceException(400, "Missing or invalid: offset");
    }

    final var limit = request.limit();
    if (limit != null && (limit < 0 || limit > 50)) {
      throw new ServiceException(400, "Missing or invalid: limit");
    }

    final var page = accountService.getRooms(sessionContext.accountId(), request);

    final var roomInfos = page.getContent().stream().map(RoomMappers::toRoomInfo).toList();

    final var response =
        new GetRoomsResponse()
            .roomInfos(roomInfos)
            .offset(offset)
            .limit(limit)
            .totalCount(page.getTotalElements());

    sessionContext.send(message.clone().data(response));
  }

  private static boolean isEmailValid(String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }
}
