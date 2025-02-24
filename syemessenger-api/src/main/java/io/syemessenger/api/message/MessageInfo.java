package io.syemessenger.api.message;

import java.time.LocalDateTime;
import java.util.StringJoiner;

public class MessageInfo {

  private Long senderId;
  private Long roomId;
  private String message;
  private LocalDateTime timestamp;

  public Long senderId() {
    return senderId;
  }

  public MessageInfo senderId(Long senderId) {
    this.senderId = senderId;
    return this;
  }

  public Long roomId() {
    return roomId;
  }

  public MessageInfo roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public String message() {
    return message;
  }

  public MessageInfo message(String message) {
    this.message = message;
    return this;
  }

  public LocalDateTime timestamp() {
    return timestamp;
  }

  public MessageInfo timestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", MessageInfo.class.getSimpleName() + "[", "]")
        .add("senderId=" + senderId)
        .add("roomId=" + roomId)
        .add("message='" + message + "'")
        .add("timestamp=" + timestamp)
        .toString();
  }
}
