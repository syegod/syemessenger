package io.syemessenger.api.room;

import static io.syemessenger.api.Pageables.toPageable;

import io.syemessenger.annotations.RequestController;
import io.syemessenger.annotations.RequestHandler;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.AccountMappers;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.api.room.repository.BlockedMember;
import io.syemessenger.api.room.repository.BlockedMemberId;
import io.syemessenger.api.room.repository.BlockedRepository;
import io.syemessenger.api.room.repository.Room;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;

@Named
@RequestController
public class RoomController {

  private final RoomRepository roomRepository;
  private final AccountRepository accountRepository;
  private final BlockedRepository blockedRepository;

  public RoomController(
      RoomRepository roomRepository,
      AccountRepository accountRepository,
      BlockedRepository blockedRepository) {
    this.roomRepository = roomRepository;
    this.accountRepository = accountRepository;
    this.blockedRepository = blockedRepository;
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

    final var account = accountRepository.findById(sessionContext.accountId()).orElse(null);
    if (account == null) {
      throw new ServiceException(404, "Account not found");
    }

    final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
    final var room =
        new Room().name(name).description(description).owner(account).createdAt(now).updatedAt(now);

    try {
      final var saved = roomRepository.save(room);
      roomRepository.saveRoomMember(saved.id(), account.id());
      final var roomInfo = RoomMappers.toRoomInfo(saved);
      sessionContext.send(new ServiceMessage().qualifier("createRoom").data(roomInfo));
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
    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!room.owner().id().equals(sessionContext.accountId())) {
      throw new ServiceException(403, "Not room owner");
    }

    room.description(description);

    final var saved = roomRepository.save(room);
    final var roomInfo = RoomMappers.toRoomInfo(saved);

    sessionContext.send(new ServiceMessage().qualifier("updateRoom").data(roomInfo));
  }

  @RequestHandler("v1/syemessenger/getRoom")
  public void getRoom(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var room = roomRepository.findById(id).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

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

    final var room = roomRepository.findByName(name);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var blockedMember =
        blockedRepository
            .findById(new BlockedMemberId().roomId(room.id()).accountId(sessionContext.accountId()))
            .orElse(null);
    if (blockedMember != null) {
      throw new ServiceException(400, "Cannot join room: blocked");
    }

    try {
      roomRepository.saveRoomMember(room.id(), sessionContext.accountId());
    } catch (DataAccessException e) {
      if (e.getMessage().contains("duplicate key value violates unique constraint")) {
        throw new ServiceException(400, "Cannot join room: already joined");
      }
      throw e;
    }

    sessionContext.send(new ServiceMessage().qualifier("joinRoom").data(room.id()));
  }

  @RequestHandler("v1/syemessenger/leaveRoom")
  public void leaveRoom(SessionContext sessionContext, Long id) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    if (id == null) {
      throw new ServiceException(400, "Missing or invalid: id");
    }

    final var room = roomRepository.findById(id).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var roomMember = roomRepository.findRoomMember(id, sessionContext.accountId());
    if (roomMember == null) {
      throw new ServiceException(400, "Cannot leave room: not joined");
    }

    if (room.owner().id().equals(sessionContext.accountId())) {
      roomRepository.deleteById(id);
      sessionContext.send(new ServiceMessage().qualifier("leaveRoom").data(id));
    } else {
      roomRepository.deleteRoomMember(id, sessionContext.accountId());
      sessionContext.send(new ServiceMessage().qualifier("leaveRoom").data(id));
    }
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

    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!sessionContext.accountId().equals(room.owner().id())) {
      throw new ServiceException(403, "Not room owner");
    }

    if (memberIds.contains(room.owner().id())) {
      throw new ServiceException(400, "Cannot remove room owner");
    }

    roomRepository.deleteRoomMembers(roomId, memberIds);

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

    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!sessionContext.accountId().equals(room.owner().id())) {
      throw new ServiceException(403, "Not room owner");
    }

    if (memberIds.contains(room.owner().id())) {
      throw new ServiceException(400, "Cannot block room owner");
    }

    final var blockedMembers = new ArrayList<BlockedMember>();
    for (var memberId : memberIds) {
      blockedMembers.add(new BlockedMember().roomId(roomId).accountId(memberId));
    }

    try {
      roomRepository.deleteRoomMembers(roomId, memberIds);
      blockedRepository.saveAll(blockedMembers);
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

    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!sessionContext.accountId().equals(room.owner().id())) {
      throw new ServiceException(403, "Not room owner");
    }

    final var blockedMembers = new ArrayList<BlockedMember>();
    for (var memberId : memberIds) {
      blockedMembers.add(new BlockedMember().roomId(roomId).accountId(memberId));
    }

    blockedRepository.deleteAll(blockedMembers);

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

    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!room.owner().id().equals(sessionContext.accountId())) {
      throw new ServiceException(403, "Not room owner");
    }

    final var pageable = toPageable(offset, limit, request.orderBy());

    final var accountPage = roomRepository.findBlockedMembers(roomId, pageable);

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

    final var pageable = toPageable(offset, limit, request.orderBy());

    var keyword = request.keyword();
    Page<Room> roomsPages;
    if (keyword == null) {
      roomsPages = roomRepository.findAll(pageable);
    } else {
      roomsPages =
          roomRepository.findByNameContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    final var roomInfos = roomsPages.getContent().stream().map(RoomMappers::toRoomInfo).toList();

    final var listRoomsResponse =
        new ListRoomsResponse()
            .roomInfos(roomInfos)
            .offset(offset)
            .limit(limit)
            .totalCount(roomsPages.getTotalElements());

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

    final var pages =
        roomRepository.findRoomMembers(
            request.roomId(), toPageable(offset, limit, request.orderBy()));

    final var accountInfos =
        pages.getContent().stream().map(AccountMappers::toAccountInfo).toList();

    GetRoomMembersResponse response =
        new GetRoomMembersResponse()
            .accountInfos(accountInfos)
            .offset(offset)
            .limit(limit)
            .totalCount(pages.getTotalElements());

    sessionContext.send(new ServiceMessage().qualifier("getRoomMembers").data(response));
  }
}
