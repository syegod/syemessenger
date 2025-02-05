package io.syemessenger.api.room;

import io.syemessenger.api.OrderBy;

public class GetBlockedMembersRequest {

  private Long roomId;
  private Integer offset;
  private Integer limit;
  private OrderBy orderBy;

  public Long roomId() {
    return roomId;
  }

  public GetBlockedMembersRequest roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public GetBlockedMembersRequest offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public GetBlockedMembersRequest limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public OrderBy orderBy() {
    return orderBy;
  }

  public GetBlockedMembersRequest orderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
    return this;
  }
}
