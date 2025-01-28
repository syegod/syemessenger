package io.syemessenger.api.account;

import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.dao.DataAccessException;

@Named
public class AccountService {

  private final AccountRepository accountRepository;

  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public void createAccount(SessionContext sessionContext, CreateAccountRequest request) {
    final var username = request.username();
    if (username == null) {
      sessionContext.sendError(400, "Missing or invalid: username");
      return;
    }
    if (username.length() < 6 || username.length() > 30) {
      sessionContext.sendError(400, "Missing or invalid: username");
      return;
    }

    final var email = request.email();
    if (email == null) {
      sessionContext.sendError(400, "Missing or invalid: email");
      return;
    }
    if (email.length() < 10 || email.length() > 50) {
      sessionContext.sendError(400, "Missing or invalid: email");
      return;
    }

    final var password = request.password();
    if (password == null) {
      sessionContext.sendError(400, "Missing or invalid: password");
      return;
    }
    if (password.length() < 6 || password.length() > 25) {
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
            .status(AccountStatus.NON_CONFIRMED)
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

  // TODO: provide authorization checks
  public void updateAccount(SessionContext sessionContext, UpdateAccountRequest request) {
    if (request.id() == null) {
      sessionContext.sendError(404, "Account not found");
      return;
    }

    final var username = request.username();
    if (username != null) {
      if (username.length() < 6 || username.length() > 30) {
        sessionContext.sendError(400, "Invalid: username");
        return;
      }
    }

    final var email = request.email();
    if (email != null) {
      if (email.length() < 10 || email.length() > 50) {
        sessionContext.sendError(400, "Invalid: email");
        return;
      }
    }

    final var password = request.password();
    if (password != null) {
      if (password.length() < 6 || password.length() > 25) {
        sessionContext.sendError(400, "Invalid: password");
        return;
      }
    }

    try {
      final var account = accountRepository.findById(request.id()).orElse(null);
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

  public void getSessionAccount(SessionContext sessionContext) {}

  public void showAccount(SessionContext sessionContext, Long id) {
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
        new ServiceMessage().qualifier("showAccount").data(toPublicAccountInfo(account)));
  }

  private static AccountInfo toAccountInfo(Account account) {
    return new AccountInfo()
        .id(account.id())
        .username(account.username())
        .email(account.email())
        .status(account.status())
        .createdAt(account.createdAt())
        .updatedAt(account.updatedAt());
  }

  private static PublicAccountInfo toPublicAccountInfo(Account account) {
    return new PublicAccountInfo().id(account.id()).username(account.username());
  }
}
