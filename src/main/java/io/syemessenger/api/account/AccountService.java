package io.syemessenger.api.account;

import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.websocket.SenderContext;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.dao.DataAccessException;

@Named
public class AccountService {

  private final AccountRepository accountRepository;

  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

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
      senderContext.send(new ServiceMessage().qualifier("createAccount").data(accountInfo));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        senderContext.sendError(400, "Cannot create account: already exists");
      } else {
        senderContext.sendError(400, "Cannot create account");
      }
    } catch (Exception e) {
      senderContext.sendError(500, e.getMessage());
    }
  }

  // TODO: provide authorization checks
  public void updateAccount(SenderContext senderContext, UpdateAccountRequest request) {
    if (request.id() == null) {
      senderContext.sendError(404, "Account not found");
      return;
    }

    final var username = request.username();
    if (username != null) {
      if (username.length() < 6 || username.length() > 30) {
        senderContext.sendError(400, "Invalid: username");
        return;
      }
    }

    final var email = request.email();
    if (email != null) {
      if (email.length() < 10 || email.length() > 50) {
        senderContext.sendError(400, "Invalid: email");
        return;
      }
    }

    final var password = request.password();
    if (password != null) {
      if (password.length() < 6 || password.length() > 25) {
        senderContext.sendError(400, "Invalid: password");
        return;
      }
    }

    try {
      final var account = accountRepository.findById(request.id()).orElse(null);
      if (account == null) {
        senderContext.sendError(404, "Account not found");
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

      senderContext.send(new ServiceMessage().qualifier("updateAccount").data(accountInfo));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        senderContext.sendError(400, "Cannot update account: already exists");
      } else {
        senderContext.sendError(400, "Cannot update account");
      }
    } catch (Exception e) {
      senderContext.sendError(500, e.getMessage());
    }
  }

  public void login(SenderContext senderContext, LoginAccountRequest request) {
    final var username = request.username();
    final var email = request.email();
    if (username != null && email != null) {
      senderContext.sendError(401, "Login failed");
      return;
    }
    if (username == null && email == null) {
      senderContext.sendError(401, "Login failed");
      return;
    }

    final var password = request.password();
    if (password == null) {
      senderContext.sendError(401, "Login failed");
      return;
    }

    final var account = accountRepository.findByEmailOrUsername(email, username);
    if (account == null) {
      senderContext.sendError(401, "Login failed");
      return;
    }

    if (!PasswordHashing.check(password, account.passwordHash())) {
      senderContext.sendError(401, "Login failed");
      return;
    }


  }

  public void getSessionAccount(SenderContext senderContext) {}

  public void showAccount(SenderContext senderContext, Long id) {
    if (id == null) {
      senderContext.sendError(404, "Account not found");
      return;
    }
    final var account = accountRepository.findById(id).orElse(null);
    if (account == null) {
      senderContext.sendError(404, "Account not found");
      return;
    }
    senderContext.send(
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
