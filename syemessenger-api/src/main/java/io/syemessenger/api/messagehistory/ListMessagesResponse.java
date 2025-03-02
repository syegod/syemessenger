package io.syemessenger.api.messagehistory;

import io.syemessenger.api.message.MessageInfo;
import java.util.List;
import java.util.StringJoiner;

public class ListMessagesResponse {

  private List<MessageInfo> messages;
  private Integer limit;
  private Integer offset;
  private Long totalCount;

  public List<MessageInfo> messages() {
    return messages;
  }

  public ListMessagesResponse messages(List<MessageInfo> messages) {
    this.messages = messages;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public ListMessagesResponse limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public ListMessagesResponse offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Long totalCount() {
    return totalCount;
  }

  public ListMessagesResponse totalCount(Long totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ListMessagesResponse.class.getSimpleName() + "[", "]")
        .add("messages=" + messages)
        .add("limit=" + limit)
        .add("offset=" + offset)
        .add("totalCount=" + totalCount)
        .toString();
  }
}
