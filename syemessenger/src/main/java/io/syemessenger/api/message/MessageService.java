package io.syemessenger.api.message;

import io.syemessenger.SubscriptionRegistry;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.room.repository.BlockedMemberId;
import io.syemessenger.api.room.repository.BlockedRepository;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.websocket.SessionContext;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

  private final RoomRepository roomRepository;
  private final BlockedRepository blockedRepository;
  private final SubscriptionRegistry subscriptionRegistry;

  public MessageService(
      RoomRepository roomRepository,
      BlockedRepository blockedRepository,
      SubscriptionRegistry subscriptionRegistry) {
    this.roomRepository = roomRepository;
    this.blockedRepository = blockedRepository;
    this.subscriptionRegistry = subscriptionRegistry;
  }

  public void subscribe(Long roomId, Long accountId, SessionContext sessionContext) {
    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var roomMember = roomRepository.findRoomMember(roomId, accountId);
    if (roomMember == null) {
      throw new ServiceException(403, "Not a room member");
    }

    final var blockedMember =
        blockedRepository
            .findById(new BlockedMemberId().roomId(roomId).accountId(accountId))
            .orElse(null);
    if (blockedMember != null) {
      throw new ServiceException(400, "Cannot subscribe: blocked");
    }

    subscriptionRegistry.subscribe(roomId, sessionContext);
  }
}
