package io.syemessenger.api.messagehistory;

import static io.syemessenger.api.Pageables.toPageable;

import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.repository.AccountRepository;
import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.api.messagehistory.repository.Message;
import io.syemessenger.api.messagehistory.repository.MessageRepository;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.websocket.SessionContext;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class MessageHistoryService {

  private final RoomRepository roomRepository;
  private final AccountRepository accountRepository;
  private final MessageRepository messageRepository;

  public MessageHistoryService(
      RoomRepository roomRepository,
      AccountRepository accountRepository,
      MessageRepository messageRepository) {
    this.roomRepository = roomRepository;
    this.accountRepository = accountRepository;
    this.messageRepository = messageRepository;
  }

  public void saveMessage(MessageInfo messageInfo) {
    final var room = roomRepository.findById(messageInfo.roomId()).orElse(null);
    if (room == null) {
      throw new RuntimeException("Room not found");
    }

    final var sender = accountRepository.findById(messageInfo.senderId()).orElse(null);
    if (sender == null) {
      throw new RuntimeException("Account not found");
    }

    final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
    try {
      messageRepository.save(
          new Message().room(room).sender(sender).message(messageInfo.message()).timestamp(now));
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  public Page<Message> listMessages(SessionContext sessionContext, ListMessagesRequest request) {
    final var room = roomRepository.findById(request.roomId()).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var roomMember =
        roomRepository.findRoomMember(request.roomId(), sessionContext.accountId());
    if (roomMember == null) {
      throw new ServiceException(403, "Not a room member");
    }

    final var pageable = toPageable(request.offset(), request.limit(), request.orderBy());

    final var keyword = request.keyword();
    Page<Message> messagePage;
    if (keyword == null) {
      messagePage = messageRepository.findAll(pageable);
    } else {
      messagePage = messageRepository.findByMessageContaining(keyword, pageable);
    }

    return messagePage;
  }
}
