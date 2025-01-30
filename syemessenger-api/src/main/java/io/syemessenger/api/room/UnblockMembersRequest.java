package io.syemessenger.api.room;

import java.util.List;
import java.util.StringJoiner;

public class UnblockMembersRequest {

  private Long roomId;
  private List<Long> memberIds;

  public Long roomId() {
    return roomId;
  }

  public UnblockMembersRequest roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public List<Long> memberIds() {
    return memberIds;
  }

  public UnblockMembersRequest memberIds(List<Long> memberIds) {
    this.memberIds = memberIds;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", UnblockMembersRequest.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("memberIds=" + memberIds)
        .toString();
  }
}
