package io.syemessenger.api.room;


import io.syemessenger.annotations.RequestController;
import io.syemessenger.annotations.RequestHandler;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.AccountMappers;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import org.springframework.dao.DataAccessException;

@Named
@RequestController
public class RoomController {

  private final RoomService roomService;

  public RoomController(RoomService roomService) {
    this.roomService = roomService;
  }

  @RequestHandler("v1/syemessenger/createRoom")
  public void createRoom(SessionContext sessionContext, CreateRoomRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var name = request.name();
    if (name == null) {
      throw new ServiceException(400, "Missing or invalid: name");
    }
    if (name.length() < 8 || name.length() > 64) {
      throw new ServiceException(400, "Missing or invalid: name");
    }

    final var description = request.description();
    if (description != null) {
      if (description.length() < 8 || description.length() > 200) {
        throw new ServiceException(400, "Missing or invalid: description");
      }
    }

    try {
      final var room = roomService.createRoom(request, sessionContext.accountId());
      sessionContext.send(
          new ServiceMessage().qualifier("createRoom").data(RoomMappers.toRoomInfo(room)));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot create room: already exists");
      }
      throw e;
    }
  }

  @RequestHandler("v1/syemessenger/updateRoom")
  public void updateRoom(SessionContext sessionContext, UpdateRoomRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var description = request.description();
    if (description == null) {
      throw new ServiceException(400, "Missing or invalid: description");
    }
    if (description.length() < 8 || description.length() > 200) {
      throw new ServiceException(400, "Missing or invalid: description");
    }

    final var roomId = request.roomId();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    final var room = roomService.updateRoom(request, sessionContext.accountId());

    sessionContext.send(
        new ServiceMessage().qualifier("updateRoom").data(RoomMappers.toRoomInfo(room)));
  }

  @RequestHandler("v1/syemessenger/getRoom")
  public void getRoom(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var room = roomService.getRoom(id);

    sessionContext.send(
        new ServiceMessage().qualifier("getRoom").data(RoomMappers.toRoomInfo(room)));
  }

  @RequestHandler("v1/syemessenger/joinRoom")
  public void joinRoom(SessionContext sessionContext, String name) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    if (name == null) {
      throw new ServiceException(400, "Missing or invalid: name");
    }
    if (name.isBlank()) {
      throw new ServiceException(400, "Missing or invalid: name");
    }

    try {
      final var roomId = roomService.joinRoom(name, sessionContext.accountId());
      sessionContext.send(new ServiceMessage().qualifier("joinRoom").data(roomId));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot join room: already joined");
      }
      throw e;
    }
  }

  @RequestHandler("v1/syemessenger/leaveRoom")
  public void leaveRoom(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var roomId = roomService.leaveRoom(id, sessionContext.accountId());

    sessionContext.send(new ServiceMessage().qualifier("leaveRoom").data(roomId));
  }

  @RequestHandler("v1/syemessenger/removeRoomMembers")
  public void removeRoomMembers(SessionContext sessionContext, RemoveMembersRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var roomId = request.roomId();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    final var memberIds = request.memberIds();
    if (memberIds == null) {
      throw new ServiceException(400, "Missing or invalid: memberIds");
    }
    if (memberIds.isEmpty()) {
      throw new ServiceException(400, "Missing or invalid: memberIds");
    }

    roomService.removeRoomMembers(request, sessionContext.accountId());

    sessionContext.send(new ServiceMessage().qualifier("removeRoomMembers").data(roomId));
  }

  @RequestHandler("v1/syemessenger/blockRoomMembers")
  public void blockRoomMembers(SessionContext sessionContext, BlockMembersRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var roomId = request.roomId();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    final var memberIds = request.memberIds();
    if (memberIds == null) {
      throw new ServiceException(400, "Missing or invalid: memberIds");
    }
    if (memberIds.isEmpty()) {
      throw new ServiceException(400, "Missing or invalid: memberIds");
    }

    try {
      roomService.blockRoomMembers(request, sessionContext.accountId());
      sessionContext.send(new ServiceMessage().qualifier("blockRoomMembers").data(roomId));
    } catch (Exception e) {
      if (e.getMessage().contains("is not present in table")) {
        throw new ServiceException(404, "Account not found");
      }
      throw e;
    }
  }

  @RequestHandler("v1/syemessenger/unblockRoomMembers")
  public void unblockRoomMembers(SessionContext sessionContext, UnblockMembersRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var roomId = request.roomId();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    final var memberIds = request.memberIds();
    if (memberIds == null) {
      throw new ServiceException(400, "Missing or invalid: memberIds");
    }
    if (memberIds.isEmpty()) {
      throw new ServiceException(400, "Missing or invalid: memberIds");
    }

    roomService.unblockRoomMembers(request, sessionContext.accountId());

    sessionContext.send(new ServiceMessage().qualifier("unblockRoomMembers").data(roomId));
  }

  @RequestHandler("v1/syemessenger/getBlockedMembers")
  public void getBlockedMembers(SessionContext sessionContext, GetBlockedMembersRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var offset = request.offset();
    if (offset != null && offset < 0) {
      throw new ServiceException(400, "Missing or invalid: offset");
    }

    final var limit = request.limit();
    if (limit != null && (limit < 0 || limit > 50)) {
      throw new ServiceException(400, "Missing or invalid: limit");
    }

    final var roomId = request.roomId();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    final var accountPage = roomService.getBlockedMembers(request, sessionContext.accountId());

    final var accountInfos =
        accountPage.getContent().stream().map(AccountMappers::toAccountInfo).toList();

    final var response =
        new GetBlockedMembersResponse()
            .accountInfos(accountInfos)
            .offset(offset)
            .limit(limit)
            .totalCount(accountPage.getTotalElements());

    sessionContext.send(new ServiceMessage().qualifier("getBlockedMembers").data(response));
  }

  @RequestHandler("v1/syemessenger/listRooms")
  public void listRooms(SessionContext sessionContext, ListRoomsRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var offset = request.offset();
    if (offset != null && offset < 0) {
      throw new ServiceException(400, "Missing or invalid: offset");
    }

    final var limit = request.limit();
    if (limit != null && (limit < 0 || limit > 50)) {
      throw new ServiceException(400, "Missing or invalid: limit");
    }

    final var roomsPage = roomService.listRooms(request);

    final var roomInfos = roomsPage.getContent().stream().map(RoomMappers::toRoomInfo).toList();

    final var listRoomsResponse =
        new ListRoomsResponse()
            .roomInfos(roomInfos)
            .offset(offset)
            .limit(limit)
            .totalCount(roomsPage.getTotalElements());

    sessionContext.send(new ServiceMessage().qualifier("listRooms").data(listRoomsResponse));
  }

  @RequestHandler("v1/syemessenger/getRoomMembers")
  public void getRoomMembers(SessionContext sessionContext, GetRoomMembersRequest request) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    Long roomId = request.roomId();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    final var offset = request.offset();
    if (offset != null && offset < 0) {
      throw new ServiceException(400, "Missing or invalid: offset");
    }

    final var limit = request.limit();
    if (limit != null && (limit < 0 || limit > 50)) {
      throw new ServiceException(400, "Missing or invalid: limit");
    }

    final var membersPage = roomService.getRoomMembers(request, sessionContext.accountId());

    final var accountInfos =
        membersPage.getContent().stream().map(AccountMappers::toAccountInfo).toList();

    GetRoomMembersResponse response =
        new GetRoomMembersResponse()
            .accountInfos(accountInfos)
            .offset(offset)
            .limit(limit)
            .totalCount(membersPage.getTotalElements());

    sessionContext.send(new ServiceMessage().qualifier("getRoomMembers").data(response));
  }
}
