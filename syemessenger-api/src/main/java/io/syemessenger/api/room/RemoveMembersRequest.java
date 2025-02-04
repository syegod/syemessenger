package io.syemessenger.api.room;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class RemoveMembersRequest {

  private Long roomId;
  private List<Long> memberIds;

  public Long roomId() {
    return roomId;
  }

  public RemoveMembersRequest roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public List<Long> memberIds() {
    return memberIds;
  }

  public RemoveMembersRequest memberIds(List<Long> memberIds) {
    this.memberIds = memberIds;
    return this;
  }

  public RemoveMembersRequest memberIds(Long... memberIds) {
    this.memberIds = Arrays.asList(memberIds);
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RemoveMembersRequest.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("memberIds=" + memberIds)
        .toString();
  }
}
