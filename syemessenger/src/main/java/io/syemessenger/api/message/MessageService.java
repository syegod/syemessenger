package io.syemessenger.api.message;

import io.syemessenger.SubscriptionRegistry;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.room.repository.BlockedMemberId;
import io.syemessenger.api.room.repository.BlockedRepository;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.kafka.KafkaMessageCodec;
import io.syemessenger.websocket.SessionContext;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

  private final KafkaTemplate<Long, ByteBuffer> kafkaTemplate;
  private final RoomRepository roomRepository;
  private final BlockedRepository blockedRepository;
  private final SubscriptionRegistry subscriptionRegistry;

  public MessageService(
      KafkaTemplate<Long, ByteBuffer> kafkaTemplate,
      RoomRepository roomRepository,
      BlockedRepository blockedRepository,
      SubscriptionRegistry subscriptionRegistry) {
    this.kafkaTemplate = kafkaTemplate;
    this.roomRepository = roomRepository;
    this.blockedRepository = blockedRepository;
    this.subscriptionRegistry = subscriptionRegistry;
  }

  public void subscribe(Long roomId, Long accountId, SessionContext sessionContext) {
    final var room = roomRepository.findById(roomId).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var blockedMember =
        blockedRepository
            .findById(new BlockedMemberId().roomId(roomId).accountId(accountId))
            .orElse(null);
    if (blockedMember != null) {
      throw new ServiceException(403, "Cannot subscribe: blocked");
    }

    final var roomMember = roomRepository.findRoomMember(roomId, accountId);
    if (roomMember == null) {
      throw new ServiceException(403, "Cannot subscribe: not a member");
    }

    subscriptionRegistry.subscribe(roomId, sessionContext);
  }

  public Long unsubscribe(SessionContext sessionContext) {
    return subscriptionRegistry.unsubscribe(sessionContext);
  }

  public Long send(SessionContext sessionContext, String messageText) {
    final var roomId = subscriptionRegistry.roomId(sessionContext);
    final var messageInfo =
        new MessageInfo().roomId(roomId).message(messageText).senderId(sessionContext.accountId());

    try {
      kafkaTemplate
          .send("messages", roomId, KafkaMessageCodec.encodeRoomMessage(messageInfo))
          .get(3, TimeUnit.SECONDS);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to send message", ex);
    }

    return roomId;
  }
}
