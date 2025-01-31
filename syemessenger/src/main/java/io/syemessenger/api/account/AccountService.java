package io.syemessenger.api.account;

import io.syemessenger.api.Pageables;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.api.room.RoomMappers;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;

@Named
public class AccountService {

  private final AccountRepository accountRepository;
  private final RoomRepository roomRepository;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  public AccountService(AccountRepository accountRepository, RoomRepository roomRepository) {
    this.accountRepository = accountRepository;
    this.roomRepository = roomRepository;
  }

  public void createAccount(SessionContext sessionContext, CreateAccountRequest request) {
    final var username = request.username();
    if (username == null) {
      sessionContext.sendError(400, "Missing or invalid: username");
      return;
    }
    if (username.length() < 8 || username.length() > 64) {
      sessionContext.sendError(400, "Missing or invalid: username");
      return;
    }

    final var email = request.email();
    if (email == null) {
      sessionContext.sendError(400, "Missing or invalid: email");
      return;
    }
    if (email.length() < 8 || email.length() > 64) {
      sessionContext.sendError(400, "Missing or invalid: email");
      return;
    }

    if (!isEmailValid(email)) {
      sessionContext.sendError(400, "Missing or invalid: email");
      return;
    }

    final var password = request.password();
    if (password == null) {
      sessionContext.sendError(400, "Missing or invalid: password");
      return;
    }
    if (password.length() < 8 || password.length() > 64) {
      sessionContext.sendError(400, "Missing or invalid: password");
      return;
    }

    final var now = LocalDateTime.now(Clock.systemUTC());

    final var hashedPassword = PasswordHashing.hash(password);

    final var account =
        new Account()
            .username(username)
            .email(email)
            .passwordHash(hashedPassword)
            .createdAt(now)
            .updatedAt(now);
    try {
      final var saved = accountRepository.save(account);
      final var accountInfo = AccountMappers.toAccountInfo(saved);
      sessionContext.send(new ServiceMessage().qualifier("createAccount").data(accountInfo));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        sessionContext.sendError(400, "Cannot create account: already exists");
      } else {
        sessionContext.sendError(400, "Cannot create account");
      }
    } catch (Exception e) {
      sessionContext.sendError(500, e.getMessage());
    }
  }

  public void updateAccount(SessionContext sessionContext, UpdateAccountRequest request) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    final var username = request.username();
    if (username != null) {
      if (username.length() < 8 || username.length() > 64) {
        sessionContext.sendError(400, "Invalid: username");
        return;
      }
    }

    final var email = request.email();
    if (email != null) {
      if (email.length() < 8 || email.length() > 64) {
        sessionContext.sendError(400, "Invalid: email");
        return;
      }
      if (!isEmailValid(email)) {
        sessionContext.sendError(400, "Invalid: email");
        return;
      }
    }

    final var password = request.password();
    if (password != null) {
      if (password.length() < 8 || password.length() > 64) {
        sessionContext.sendError(400, "Invalid: password");
        return;
      }
    }

    try {
      final var account = accountRepository.findById(sessionContext.accountId()).orElse(null);
      if (account == null) {
        sessionContext.sendError(404, "Account not found");
        return;
      }

      if (username != null) {
        account.username(username);
      }
      if (email != null) {
        account.email(email);
      }
      if (password != null) {
        account.passwordHash(PasswordHashing.hash(password));
      }

      final var updated =
          accountRepository.save(account.updatedAt(LocalDateTime.now(Clock.systemUTC())));
      final var accountInfo = AccountMappers.toAccountInfo(updated);

      sessionContext.send(new ServiceMessage().qualifier("updateAccount").data(accountInfo));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        sessionContext.sendError(400, "Cannot update account: already exists");
      } else {
        sessionContext.sendError(400, "Cannot update account");
      }
    } catch (Exception e) {
      sessionContext.sendError(500, e.getMessage());
    }
  }

  public void login(SessionContext sessionContext, LoginAccountRequest request) {
    final var username = request.username();
    final var email = request.email();
    if (username != null && email != null) {
      sessionContext.sendError(401, "Login failed");
      return;
    }
    if (username == null && email == null) {
      sessionContext.sendError(401, "Login failed");
      return;
    }

    final var password = request.password();
    if (password == null) {
      sessionContext.sendError(401, "Login failed");
      return;
    }

    final var account = accountRepository.findByEmailOrUsername(email, username);
    if (account == null) {
      sessionContext.sendError(401, "Login failed");
      return;
    }

    if (!PasswordHashing.check(password, account.passwordHash())) {
      sessionContext.sendError(401, "Login failed");
      return;
    }

    sessionContext.accountId(account.id());
    sessionContext.send(new ServiceMessage().qualifier("login").data(account.id()));
  }

  public void getAccount(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    Account account;

    if (id == null) {
      account = accountRepository.findById(sessionContext.accountId()).orElse(null);
    } else {
      account = accountRepository.findById(id).orElse(null);
    }

    if (account == null) {
      sessionContext.sendError(404, "Account not found");
      return;
    }

    sessionContext.send(
        new ServiceMessage().qualifier("getAccount").data(AccountMappers.toAccountInfo(account)));
  }

  public void getRooms(SessionContext sessionContext, GetRoomsRequest request) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    final var offset = request.offset();
    if (offset != null && offset < 0) {
      sessionContext.sendError(400, "Missing or invalid: offset");
      return;
    }

    final var limit = request.limit();
    if (limit != null && (limit < 0 || limit > 50)) {
      sessionContext.sendError(400, "Missing or invalid: limit");
      return;
    }

    final var roomPage =
        roomRepository.findByAccountId(
            sessionContext.accountId(), Pageables.toPageable(offset, limit, request.orderBy()));

    final var roomInfos = roomPage.getContent().stream().map(RoomMappers::toRoomInfo).toList();

    final var getRoomsResponse =
        new GetRoomsResponse()
            .roomInfos(roomInfos)
            .offset(offset)
            .limit(limit)
            .totalCount(roomPage.getTotalElements());

    sessionContext.send(new ServiceMessage().qualifier("getRooms").data(getRoomsResponse));
  }

  private static boolean isEmailValid(String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }
}
