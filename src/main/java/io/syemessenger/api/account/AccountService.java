package io.syemessenger.api.account;

import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.websocket.SenderContext;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

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
    // TODO: passwordHash
    final var account =
        new Account()
            .username(username)
            .email(email)
            .passwordHash(password)
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

  public void updateAccount(SenderContext senderContext, UpdateAccountRequest request) {}

  public void login(SenderContext senderContext, LoginAccountRequest request) {}

  public void getSessionAccount(SenderContext senderContext) {}

  public void showAccount(SenderContext senderContext, Long id) {}

  private static AccountInfo toAccountInfo(Account account) {
    return new AccountInfo()
        .id(account.id())
        .username(account.username())
        .email(account.email())
        .status(account.status())
        .createdAt(account.createdAt())
        .updatedAt(account.updatedAt());
  }
}
