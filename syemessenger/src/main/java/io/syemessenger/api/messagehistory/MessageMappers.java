package io.syemessenger.api.messagehistory;

import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.api.messagehistory.repository.Message;

public class MessageMappers {

  public static MessageInfo toMessageInfo(Message message) {
    return new MessageInfo()
        .id(message.id())
        .roomId(message.room().id())
        .senderId(message.sender().id())
        .message(message.message())
        .timestamp(message.timestamp());
  }
}
