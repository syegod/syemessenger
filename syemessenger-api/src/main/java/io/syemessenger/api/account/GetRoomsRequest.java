package io.syemessenger.api.account;

import io.syemessenger.api.OrderBy;
import java.util.StringJoiner;

public class GetRoomsRequest {

  private Integer offset;
  private Integer limit;
  private OrderBy orderBy;

  public Integer offset() {
    return offset;
  }

  public GetRoomsRequest offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public GetRoomsRequest limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public OrderBy orderBy() {
    return orderBy;
  }

  public GetRoomsRequest orderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GetRoomsRequest.class.getSimpleName() + "[", "]")
        .add("offset=" + offset)
        .add("limit=" + limit)
        .add("orderBy=" + orderBy)
        .toString();
  }
}
