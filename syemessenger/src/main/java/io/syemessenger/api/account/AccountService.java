package io.syemessenger.api.account;

import io.syemessenger.api.Pageables;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.api.room.RoomMappers;
import io.syemessenger.api.room.repository.RoomRepository;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@Named
@Transactional
public class AccountService {

  private final AccountRepository accountRepository;
  private final RoomRepository roomRepository;

  public AccountService(AccountRepository accountRepository, RoomRepository roomRepository) {
    this.accountRepository = accountRepository;
    this.roomRepository = roomRepository;
  }

  public AccountInfo createAccount(CreateAccountRequest request) {
    final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
    final var hashedPassword = PasswordHashing.hash(request.password());

    final var account =
        new Account()
            .username(request.username())
            .email(request.email())
            .passwordHash(hashedPassword)
            .createdAt(now)
            .updatedAt(now);
    try {
      final var saved = accountRepository.save(account);
      return AccountMappers.toAccountInfo(saved);
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot create account: already exists");
      }
      throw e;
    }
  }

  public AccountInfo updateAccount(UpdateAccountRequest request, Long accountId) {
    try {
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

      final var updated =
          accountRepository.save(account.updatedAt(LocalDateTime.now(Clock.systemUTC())));

      return AccountMappers.toAccountInfo(updated);
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot update account: already exists");
      }
      throw e;
    }
  }

  @Transactional(readOnly = true)
  public Long login(LoginAccountRequest request) {
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
  public AccountInfo getAccount(Long id) {
    final var account = accountRepository.findById(id).orElse(null);

    if (account == null) {
      throw new ServiceException(404, "Account not found");
    }

    return AccountMappers.toAccountInfo(account);
  }

  @Transactional(readOnly = true)
  public GetRoomsResponse getRooms(Long id, GetRoomsRequest request) {
    final var offset = request.offset();
    final var limit = request.limit();

    final var roomPage =
        roomRepository.findByAccountId(id, Pageables.toPageable(offset, limit, request.orderBy()));

    final var roomInfos = roomPage.getContent().stream().map(RoomMappers::toRoomInfo).toList();

    return new GetRoomsResponse()
        .roomInfos(roomInfos)
        .offset(offset)
        .limit(limit)
        .totalCount(roomPage.getTotalElements());
  }
}
