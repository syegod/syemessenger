package io.syemessenger.api;

import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.api.room.BlockMembersRequest;
import io.syemessenger.api.room.CreateRoomRequest;
import io.syemessenger.api.room.ListRoomsRequest;
import io.syemessenger.api.room.RemoveMembersRequest;
import io.syemessenger.api.room.RoomInfo;
import io.syemessenger.api.room.UnblockMembersRequest;
import io.syemessenger.api.room.UpdateRoomRequest;
import io.syemessenger.api.room.repository.Room;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.dao.DataAccessException;

@Named
public class RoomService {

  private final RoomRepository roomRepository;
  private final AccountRepository accountRepository;

  public RoomService(RoomRepository roomRepository, AccountRepository accountRepository) {
    this.roomRepository = roomRepository;
    this.accountRepository = accountRepository;
  }

  public void createRoom(SessionContext sessionContext, CreateRoomRequest request) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    final var name = request.name();
    if (name == null) {
      sessionContext.sendError(400, "Missing or invalid: name");
      return;
    }
    if (name.length() < 8 || name.length() > 64) {
      sessionContext.sendError(400, "Missing or invalid: name");
      return;
    }

    final var description = request.description();
    if (description != null) {
      if (description.length() < 8 || description.length() > 200) {
        sessionContext.sendError(400, "Missing or invalid: description");
        return;
      }
    }

    final var account = accountRepository.findById(sessionContext.accountId()).orElse(null);
    if (account == null) {
      sessionContext.sendError(404, "Account not found");
      return;
    }

    final var now = LocalDateTime.now(Clock.systemUTC());
    final var room =
        new Room().name(name).description(description).owner(account).createdAt(now).updatedAt(now);

    try {
      final var saved = roomRepository.save(room);
      final var roomInfo = toRoomInfo(saved);
      sessionContext.send(new ServiceMessage().qualifier("createRoom").data(roomInfo));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        sessionContext.sendError(400, "Cannot create room: already exists");
      } else {
        sessionContext.sendError(400, "Cannot create room");
      }
    } catch (Exception e) {
      sessionContext.sendError(500, e.getMessage());
    }
  }

  public void updateRoom(SessionContext sessionContext, UpdateRoomRequest request) {}

  public void getRoom(SessionContext sessionContext, Long id) {}

  public void joinRoom(SessionContext sessionContext, String name) {}

  public void leaveRoom(SessionContext sessionContext, Long id) {}

  public void removeRoomMembers(SessionContext sessionContext, RemoveMembersRequest request) {}

  public void blockRoomMembers(SessionContext sessionContext, BlockMembersRequest request) {}

  public void unblockRoomMembers(SessionContext sessionContext, UnblockMembersRequest request) {}

  public void listRooms(SessionContext sessionContext, ListRoomsRequest request) {}

  private static RoomInfo toRoomInfo(Room room) {
    return new RoomInfo()
        .id(room.id())
        .name(room.name())
        .owner(room.owner().username())
        .description(room.description())
        .createdAt(room.createdAt())
        .updatedAt(room.updatedAt());
  }
}
