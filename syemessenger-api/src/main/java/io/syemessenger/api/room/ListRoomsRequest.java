package io.syemessenger.api.room;

import io.syemessenger.api.OrderBy;
import java.util.StringJoiner;

public class ListRoomsRequest {

  private String keyword;
  private Integer offset;
  private Integer limit;
  private OrderBy orderBy;

  public String keyword() {
    return keyword;
  }

  public ListRoomsRequest keyword(String keyword) {
    this.keyword = keyword;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public ListRoomsRequest offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public ListRoomsRequest limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public OrderBy orderBy() {
    return orderBy;
  }

  public ListRoomsRequest orderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ListRoomsRequest.class.getSimpleName() + "[", "]")
        .add("keyword='" + keyword + "'")
        .add("offset=" + offset)
        .add("limit=" + limit)
        .add("orderBy=" + orderBy)
        .toString();
  }
}
