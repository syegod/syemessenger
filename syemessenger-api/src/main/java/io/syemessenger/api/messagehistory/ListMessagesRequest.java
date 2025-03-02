package io.syemessenger.api.messagehistory;

import io.syemessenger.api.OrderBy;
import java.time.LocalDateTime;
import java.util.StringJoiner;

public class ListMessagesRequest {

  private Long roomId;
  private LocalDateTime from;
  private LocalDateTime to;
  private String timezone;
  private String keyword;
  private Integer limit;
  private Integer offset;
  private OrderBy orderBy;

  public Long roomId() {
    return roomId;
  }

  public ListMessagesRequest roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public LocalDateTime from() {
    return from;
  }

  public ListMessagesRequest from(LocalDateTime from) {
    this.from = from;
    return this;
  }

  public LocalDateTime to() {
    return to;
  }

  public ListMessagesRequest to(LocalDateTime to) {
    this.to = to;
    return this;
  }

  public String timezone() {
    return timezone;
  }

  public ListMessagesRequest timezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  public String keyword() {
    return keyword;
  }

  public ListMessagesRequest keyword(String keyword) {
    this.keyword = keyword;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public ListMessagesRequest limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public ListMessagesRequest offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public OrderBy orderBy() {
    return orderBy;
  }

  public ListMessagesRequest orderBy(OrderBy orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ListMessagesRequest.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("from=" + from)
        .add("to=" + to)
        .add("timezone='" + timezone + "'")
        .add("keyword='" + keyword + "'")
        .add("limit=" + limit)
        .add("offset=" + offset)
        .add("orderBy=" + orderBy)
        .toString();
  }
}
