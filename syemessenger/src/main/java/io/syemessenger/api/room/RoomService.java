package io.syemessenger.api.room;

import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.repository.AccountRepository;
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
      roomRepository.saveRoomMember(saved.id(), account.id());
      final var roomInfo = RoomMappers.toRoomInfo(saved);
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

  public void updateRoom(SessionContext sessionContext, UpdateRoomRequest request) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    final var description = request.description();
    if (description == null) {
      sessionContext.sendError(400, "Missing or invalid: description");
      return;
    }
    if (description.length() < 8 || description.length() > 200) {
      sessionContext.sendError(400, "Missing or invalid: description");
      return;
    }

    final var roomId = request.roomId();
    if (roomId == null) {
      sessionContext.sendError(400, "Missing or invalid: roomId");
      return;
    }
    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      sessionContext.sendError(404, "Room not found");
      return;
    }

    if (!room.owner().id().equals(sessionContext.accountId())) {
      sessionContext.sendError(403, "Not allowed");
      return;
    }

    room.description(description);

    try {
      final var saved = roomRepository.save(room);
      final var roomInfo = RoomMappers.toRoomInfo(saved);
      sessionContext.send(new ServiceMessage().qualifier("updateRoom").data(roomInfo));
    } catch (Exception e) {
      sessionContext.sendError(500, e.getMessage());
    }
  }

  public void getRoom(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    if (id == null) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    final var room = roomRepository.findById(id).orElse(null);
    if (room == null) {
      sessionContext.sendError(404, "Room not found");
      return;
    }

    sessionContext.send(
        new ServiceMessage().qualifier("getRoom").data(RoomMappers.toRoomInfo(room)));
  }

  public void joinRoom(SessionContext sessionContext, String name) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    if (name == null) {
      sessionContext.sendError(400, "Missing or invalid: name");
      return;
    }
    if (name.isBlank()) {
      sessionContext.sendError(400, "Missing or invalid: name");
      return;
    }

    final var room = roomRepository.findByName(name);
    if (room == null) {
      sessionContext.sendError(404, "Room not found");
      return;
    }

    try {
      roomRepository.saveRoomMember(room.id(), sessionContext.accountId());
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        sessionContext.sendError(400, "Cannot join room: already joined");
      } else {
        sessionContext.sendError(400, "Cannot create room");
      }
    } catch (Exception e) {
      sessionContext.sendError(500, e.getMessage());
    }

    sessionContext.send(new ServiceMessage().qualifier("joinRoom").data(room.id()));
  }

  public void leaveRoom(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      sessionContext.sendError(401, "Not authenticated");
      return;
    }

    if (id == null) {
      sessionContext.sendError(400, "Missing or invalid: id");
      return;
    }

    final var room = roomRepository.findById(id).orElse(null);
    if (room == null) {
      sessionContext.sendError(404, "Room not found");
      return;
    }

    try {
      roomRepository.deleteRoomMember(id, sessionContext.accountId());
      sessionContext.send(new ServiceMessage().qualifier("leaveRoom").data(id));
    } catch (Exception e) {
      sessionContext.sendError(500, e.getMessage());
    }
  }

  public void removeRoomMembers(SessionContext sessionContext, RemoveMembersRequest request) {}

  public void blockRoomMembers(SessionContext sessionContext, BlockMembersRequest request) {}

  public void unblockRoomMembers(SessionContext sessionContext, UnblockMembersRequest request) {}

  public void listRooms(SessionContext sessionContext, ListRoomsRequest request) {}

  public void getRoomMembers(SessionContext sessionContext, GetRoomMembersRequest request) {}
}
