package io.syemessenger.api.account;

import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;

@Named
public class AccountService {

  private final AccountRepository accountRepository;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
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
      final var accountInfo = toAccountInfo(saved);
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
      final var accountInfo = toAccountInfo(updated);

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

  public void getSessionAccount(SessionContext sessionContext) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    final Account account = accountRepository.findById(sessionContext.accountId()).orElse(null);
    if (account == null) {
      sessionContext.sendError(404, "Account not found");
      return;
    }

    sessionContext.send(
        new ServiceMessage().qualifier("getSessionAccount").data(toAccountInfo(account)));
  }

  public void showAccount(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    if (id == null) {
      sessionContext.sendError(404, "Account not found");
      return;
    }
    final var account = accountRepository.findById(id).orElse(null);
    if (account == null) {
      sessionContext.sendError(404, "Account not found");
      return;
    }
    sessionContext.send(
        new ServiceMessage().qualifier("showAccount").data(toAccountViewInfo(account)));
  }

  public void getRooms(SessionContext sessionContext, GetRoomsRequest request) {}

  private static AccountInfo toAccountInfo(Account account) {
    return new AccountInfo()
        .id(account.id())
        .username(account.username())
        .email(account.email())
        .createdAt(account.createdAt())
        .updatedAt(account.updatedAt());
  }

  private static AccountViewInfo toAccountViewInfo(Account account) {
    return new AccountViewInfo().id(account.id()).username(account.username());
  }

  private static boolean isEmailValid(String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }
}
