package io.syemessenger.kafka.dto;

import java.util.StringJoiner;

public class LeaveRoomEvent {
  private Long roomId;
  private Long accountId;
  private boolean isOwner;

  public Long roomId() {
    return roomId;
  }

  public LeaveRoomEvent roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public Long accountId() {
    return accountId;
  }

  public LeaveRoomEvent accountId(Long accountId) {
    this.accountId = accountId;
    return this;
  }

  public boolean isOwner() {
    return isOwner;
  }

  public LeaveRoomEvent isOwner(boolean owner) {
    isOwner = owner;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", LeaveRoomEvent.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("accountId=" + accountId)
        .add("isOwner=" + isOwner)
        .toString();
  }
}
