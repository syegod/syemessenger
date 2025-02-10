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

  @RequestHandler(value = "v1/syemessenger/createRoom", requestType = CreateRoomRequest.class)
  public void createRoom(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (CreateRoomRequest) message.data();

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
      sessionContext.send(message.clone().data(RoomMappers.toRoomInfo(room)));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot create room: already exists");
      }
      throw e;
    }
  }

  @RequestHandler(value = "v1/syemessenger/updateRoom", requestType = UpdateRoomRequest.class)
  public void updateRoom(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (UpdateRoomRequest) message.data();

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

    sessionContext.send(message.clone().data(RoomMappers.toRoomInfo(room)));
  }

  @RequestHandler(value = "v1/syemessenger/getRoom", requestType = Long.class)
  public void getRoom(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var id = (Long) message.data();

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var room = roomService.getRoom(id);

    sessionContext.send(message.clone().data(RoomMappers.toRoomInfo(room)));
  }

  @RequestHandler(value = "v1/syemessenger/joinRoom", requestType = String.class)
  public void joinRoom(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var name = (String) message.data();

    if (name == null) {
      throw new ServiceException(400, "Missing or invalid: name");
    }
    if (name.isBlank()) {
      throw new ServiceException(400, "Missing or invalid: name");
    }

    try {
      final var roomId = roomService.joinRoom(name, sessionContext.accountId());
      sessionContext.send(message.clone().data(roomId));
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot join room: already joined");
      }
      throw e;
    }
  }

  @RequestHandler(value = "v1/syemessenger/leaveRoom", requestType = Long.class)
  public void leaveRoom(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var id = (Long) message.data();

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var roomId = roomService.leaveRoom(id, sessionContext.accountId());

    sessionContext.send(message.clone().data(roomId));
  }

  @RequestHandler(
      value = "v1/syemessenger/removeRoomMembers",
      requestType = RemoveMembersRequest.class)
  public void removeRoomMembers(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (RemoveMembersRequest) message.data();

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

    sessionContext.send(message.clone().data(roomId));
  }

  @RequestHandler(
      value = "v1/syemessenger/blockRoomMembers",
      requestType = BlockMembersRequest.class)
  public void blockRoomMembers(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (BlockMembersRequest) message.data();

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
      sessionContext.send(message.clone().data(roomId));
    } catch (Exception e) {
      if (e.getMessage().contains("is not present in table")) {
        throw new ServiceException(404, "Account not found");
      }
      throw e;
    }
  }

  @RequestHandler(
      value = "v1/syemessenger/unblockRoomMembers",
      requestType = UnblockMembersRequest.class)
  public void unblockRoomMembers(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (UnblockMembersRequest) message.data();

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

    sessionContext.send(message.clone().data(roomId));
  }

  @RequestHandler(
      value = "v1/syemessenger/getBlockedMembers",
      requestType = GetBlockedMembersRequest.class)
  public void getBlockedMembers(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (GetBlockedMembersRequest) message.data();

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

    sessionContext.send(message.clone().data(response));
  }

  @RequestHandler(value = "v1/syemessenger/listRooms", requestType = ListRoomsRequest.class)
  public void listRooms(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (ListRoomsRequest) message.data();

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

    sessionContext.send(message.clone().data(listRoomsResponse));
  }

  @RequestHandler(
      value = "v1/syemessenger/getRoomMembers",
      requestType = GetRoomMembersRequest.class)
  public void getRoomMembers(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (GetRoomMembersRequest) message.data();

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

    sessionContext.send(message.clone().data(response));
  }
}
