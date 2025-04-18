package io.syemessenger.api.room;

import static io.syemessenger.api.Pageables.toPageable;

import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.api.room.outbox.repository.OutboxRoomEvent;
import io.syemessenger.api.room.outbox.repository.RoomEventRepository;
import io.syemessenger.api.room.repository.BlockedMember;
import io.syemessenger.api.room.repository.BlockedMemberId;
import io.syemessenger.api.room.repository.BlockedRepository;
import io.syemessenger.api.room.repository.Room;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.kafka.KafkaMessageCodec;
import io.syemessenger.kafka.dto.BlockMembersEvent;
import io.syemessenger.kafka.dto.LeaveRoomEvent;
import io.syemessenger.kafka.dto.RemoveMembersEvent;
import jakarta.inject.Named;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@Named
@Transactional
public class RoomService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RoomService.class);

  private final RoomRepository roomRepository;
  private final AccountRepository accountRepository;
  private final BlockedRepository blockedRepository;
  private final RoomEventRepository roomEventRepository;

  public RoomService(
      RoomRepository roomRepository,
      AccountRepository accountRepository,
      BlockedRepository blockedRepository,
      RoomEventRepository roomEventRepository) {
    this.roomRepository = roomRepository;
    this.accountRepository = accountRepository;
    this.blockedRepository = blockedRepository;
    this.roomEventRepository = roomEventRepository;
  }

  public Room createRoom(CreateRoomRequest request, Long accountId) {
    LOGGER.debug("Create: {}, ownerId: {}", request, accountId);
    final var account = accountRepository.findById(accountId).orElse(null);
    if (account == null) {
      throw new ServiceException(404, "Account not found");
    }

    final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
    final var room =
        new Room()
            .name(request.name())
            .description(request.description())
            .owner(account)
            .createdAt(now)
            .updatedAt(now);

    final var saved = roomRepository.save(room);
    roomRepository.saveRoomMember(saved.id(), account.id());
    return saved;
  }

  public Room updateRoom(UpdateRoomRequest request, Long accountId) {
    LOGGER.debug("Update: {}", request);
    final var room = roomRepository.findById(request.roomId()).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!room.owner().id().equals(accountId)) {
      throw new ServiceException(403, "Not room owner");
    }

    room.description(request.description());

    return roomRepository.save(room);
  }

  @Transactional(readOnly = true)
  public Room getRoom(Long id) {
    LOGGER.debug("Get: {}", id);
    final var room = roomRepository.findById(id).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    return room;
  }

  public Long joinRoom(String name, Long accountId) {
    LOGGER.debug("Join: name: {}, accountId: {}", name, accountId);
    final var room = roomRepository.findByName(name);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var blockedMember =
        blockedRepository
            .findById(new BlockedMemberId().roomId(room.id()).accountId(accountId))
            .orElse(null);
    if (blockedMember != null) {
      throw new ServiceException(400, "Cannot join room: blocked");
    }

    roomRepository.saveRoomMember(room.id(), accountId);

    return room.id();
  }

  public Long leaveRoom(Long roomId, Long accountId) {
    LOGGER.debug("Leave: roomId: {}, accountId: {}", roomId, accountId);
    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var roomMember = roomRepository.findRoomMember(roomId, accountId);
    if (roomMember == null) {
      throw new ServiceException(400, "Not a room member");
    }

    if (room.owner().id().equals(accountId)) {
      roomRepository.deleteById(roomId);

      final var encodedLeaveRoomEvent =
          KafkaMessageCodec.encodeLeaveRoomEvent(
              new LeaveRoomEvent().roomId(roomId).accountId(accountId).isOwner(true));

      roomEventRepository.save(
          new OutboxRoomEvent().roomId(roomId).data(encodedLeaveRoomEvent.array()));
    } else {
      roomRepository.deleteRoomMember(roomId, accountId);

      final var encodedLeaveRoomEvent =
          KafkaMessageCodec.encodeLeaveRoomEvent(
              new LeaveRoomEvent().roomId(roomId).accountId(accountId).isOwner(false));

      roomEventRepository.save(
          new OutboxRoomEvent().roomId(roomId).data(encodedLeaveRoomEvent.array()));
    }
    return roomId;
  }

  public Long removeRoomMembers(RemoveMembersRequest request, Long accountId) {
    LOGGER.debug("Remove members: {}", request);
    final var room = roomRepository.findById(request.roomId()).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!room.owner().id().equals(accountId)) {
      throw new ServiceException(403, "Not room owner");
    }

    if (request.memberIds().contains(room.owner().id())) {
      throw new ServiceException(400, "Cannot remove room owner");
    }

    roomRepository.deleteRoomMembers(request.roomId(), request.memberIds());

    final var encodedRemoveMembersEvent =
        KafkaMessageCodec.encodeRemoveMembersEvent(
            new RemoveMembersEvent().roomId(request.roomId()).memberIds(request.memberIds()));
    roomEventRepository.save(
        new OutboxRoomEvent().roomId(request.roomId()).data(encodedRemoveMembersEvent.array()));
    return request.roomId();
  }

  public Long blockRoomMembers(BlockMembersRequest request, Long accountId) {
    LOGGER.debug("Block members: {}", request);
    final var room = roomRepository.findById(request.roomId()).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!room.owner().id().equals(accountId)) {
      throw new ServiceException(403, "Not room owner");
    }

    if (request.memberIds().contains(room.owner().id())) {
      throw new ServiceException(400, "Cannot block room owner");
    }

    final var blockedMembers = new ArrayList<BlockedMember>();
    for (var memberId : request.memberIds()) {
      blockedMembers.add(new BlockedMember().roomId(request.roomId()).accountId(memberId));
    }

    roomRepository.deleteRoomMembers(request.roomId(), request.memberIds());
    blockedRepository.saveAll(blockedMembers);

    final var encodedBlockMembersEvent =
        KafkaMessageCodec.encodeBlockMembersEvent(
            new BlockMembersEvent().roomId(request.roomId()).memberIds(request.memberIds()));
    roomEventRepository.save(
        new OutboxRoomEvent().roomId(request.roomId()).data(encodedBlockMembersEvent.array()));

    return room.id();
  }

  public Long unblockRoomMembers(UnblockMembersRequest request, Long accountId) {
    LOGGER.debug("Unblock members: {}", request);
    final var room = roomRepository.findById(request.roomId()).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!room.owner().id().equals(accountId)) {
      throw new ServiceException(403, "Not room owner");
    }

    final var blockedMembers = new ArrayList<BlockedMember>();
    for (var memberId : request.memberIds()) {
      blockedMembers.add(new BlockedMember().roomId(request.roomId()).accountId(memberId));
    }

    blockedRepository.deleteAll(blockedMembers);
    return request.roomId();
  }

  @Transactional(readOnly = true)
  public Page<Account> getBlockedMembers(GetBlockedMembersRequest request, Long accountId) {
    LOGGER.debug("Get blocked members: {}", request);
    final var room = roomRepository.findById(request.roomId()).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    if (!room.owner().id().equals(accountId)) {
      throw new ServiceException(403, "Not room owner");
    }

    final var pageable = toPageable(request.offset(), request.limit(), request.orderBy());

    return roomRepository.findBlockedMembers(request.roomId(), pageable);
  }

  @Transactional(readOnly = true)
  public Page<Room> listRooms(ListRoomsRequest request) {
    LOGGER.debug("List rooms: {}", request);
    final var pageable = toPageable(request.offset(), request.limit(), request.orderBy());

    var keyword = request.keyword();
    Page<Room> roomPage;
    if (keyword == null) {
      roomPage = roomRepository.findAll(pageable);
    } else {
      roomPage =
          roomRepository.findByNameContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    return roomPage;
  }

  @Transactional(readOnly = true)
  public Page<Account> getRoomMembers(GetRoomMembersRequest request, Long accountId) {
    LOGGER.debug("Get members: {}", request);
    final var roomMember = roomRepository.findRoomMember(request.roomId(), accountId);
    if (roomMember == null) {
      throw new ServiceException(403, "Not a room member");
    }

    return roomRepository.findRoomMembers(
        request.roomId(), toPageable(request.offset(), request.limit(), request.orderBy()));
  }
}
