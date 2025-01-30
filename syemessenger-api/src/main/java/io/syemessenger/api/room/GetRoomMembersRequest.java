package io.syemessenger.api.room;

import io.syemessenger.api.OrderBy;
import java.util.StringJoiner;

public class GetRoomMembersRequest {

  private Long roomId;
  private Integer offset;
  private Integer limit;
  private OrderBy orderBy;

  public Long roomId() {
    return roomId;
  }

  public GetRoomMembersRequest roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public GetRoomMembersRequest offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public GetRoomMembersRequest limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public OrderBy orderBy() {
    return orderBy;
  }

  public GetRoomMembersRequest orderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GetRoomMembersRequest.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("offset=" + offset)
        .add("limit=" + limit)
        .add("orderBy=" + orderBy)
        .toString();
  }
}
