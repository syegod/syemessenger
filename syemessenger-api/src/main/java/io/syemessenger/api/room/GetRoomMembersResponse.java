package io.syemessenger.api.room;

import io.syemessenger.api.account.AccountInfo;
import java.util.List;
import java.util.StringJoiner;

public class GetRoomMembersResponse {

  private List<AccountInfo> accountInfos;
  private Integer offset;
  private Integer limit;
  private Long totalCount;

  public List<AccountInfo> accountInfos() {
    return accountInfos;
  }

  public GetRoomMembersResponse accountInfos(List<AccountInfo> accountInfos) {
    this.accountInfos = accountInfos;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public GetRoomMembersResponse offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public GetRoomMembersResponse limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Long totalCount() {
    return totalCount;
  }

  public GetRoomMembersResponse totalCount(Long totalCount) {
    this.totalCount = totalCount;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GetRoomMembersResponse.class.getSimpleName() + "[", "]")
        .add("accountInfos=" + accountInfos)
        .add("offset=" + offset)
        .add("limit=" + limit)
        .add("totalCount=" + totalCount)
        .toString();
  }
}
