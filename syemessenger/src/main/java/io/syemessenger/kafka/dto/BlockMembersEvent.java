package io.syemessenger.kafka.dto;

import java.util.List;
import java.util.StringJoiner;

public class BlockMembersEvent {
  private Long roomId;
  private List<Long> memberIds;

  public Long roomId() {
    return roomId;
  }

  public BlockMembersEvent roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public List<Long> memberIds() {
    return memberIds;
  }

  public BlockMembersEvent memberIds(List<Long> memberIds) {
    this.memberIds = memberIds;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BlockMembersEvent.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("memberIds=" + memberIds)
        .toString();
  }
}
