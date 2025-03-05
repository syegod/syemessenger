package io.syemessenger.api.account;

import io.syemessenger.api.Pageables;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.api.room.repository.Room;
import io.syemessenger.api.room.repository.RoomRepository;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@Named
@Transactional
public class AccountService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

  private final AccountRepository accountRepository;
  private final RoomRepository roomRepository;

  public AccountService(AccountRepository accountRepository, RoomRepository roomRepository) {
    this.accountRepository = accountRepository;
    this.roomRepository = roomRepository;
  }

  public Account createAccount(CreateAccountRequest request) {
    LOGGER.debug("Create: {}", request);
    final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
    final var hashedPassword = PasswordHashing.hash(request.password());

    final var account =
        new Account()
            .username(request.username())
            .email(request.email())
            .passwordHash(hashedPassword)
            .createdAt(now)
            .updatedAt(now);

    return accountRepository.save(account);
  }

  public Account updateAccount(UpdateAccountRequest request, Long accountId) {
    LOGGER.debug("Update: {} with data {}", accountId, request);
    final var account = accountRepository.findById(accountId).orElse(null);
    if (account == null) {
      throw new ServiceException(404, "Account not found");
    }

    if (request.username() != null) {
      account.username(request.username());
    }
    if (request.email() != null) {
      account.email(request.email());
    }
    if (request.password() != null) {
      account.passwordHash(PasswordHashing.hash(request.password()));
    }

    Account updated = account.updatedAt(LocalDateTime.now(Clock.systemUTC()));
    return accountRepository.save(updated);
  }

  @Transactional(readOnly = true)
  public Long login(LoginAccountRequest request) {
    LOGGER.debug("Login: {}", request);
    final var account =
        accountRepository.findByEmailOrUsername(request.email(), request.username());
    if (account == null) {
      throw new ServiceException(401, "Login failed");
    }

    if (!PasswordHashing.check(request.password(), account.passwordHash())) {
      throw new ServiceException(401, "Login failed");
    }

    return account.id();
  }

  @Transactional(readOnly = true)
  public Account getAccount(Long id) {
    LOGGER.debug("Get: {}", id);
    final var account = accountRepository.findById(id).orElse(null);

    if (account == null) {
      throw new ServiceException(404, "Account not found");
    }

    return account;
  }

  @Transactional(readOnly = true)
  public Page<Room> getRooms(Long id, GetRoomsRequest request) {
    LOGGER.debug("Get rooms of account {}: {}", id, request);
    final var offset = request.offset();
    final var limit = request.limit();

    return roomRepository.findByAccountId(
        id, Pageables.toPageable(offset, limit, request.orderBy()));
  }
}
