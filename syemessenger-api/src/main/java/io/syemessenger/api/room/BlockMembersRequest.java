package io.syemessenger.api.room;

import java.util.List;
import java.util.StringJoiner;

public class BlockMembersRequest {

  private Long roomId;
  private List<Long> memberIds;

  public Long roomId() {
    return roomId;
  }

  public BlockMembersRequest roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public List<Long> memberIds() {
    return memberIds;
  }

  public BlockMembersRequest memberIds(List<Long> memberIds) {
    this.memberIds = memberIds;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BlockMembersRequest.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("memberIds=" + memberIds)
        .toString();
  }
}
