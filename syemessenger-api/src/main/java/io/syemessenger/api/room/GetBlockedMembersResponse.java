package io.syemessenger.api.room;

import io.syemessenger.api.account.AccountInfo;
import java.util.Arrays;
import java.util.List;

public class GetBlockedMembersResponse {
  private List<AccountInfo> accountInfos;
  private Integer offset;
  private Integer limit;
  private Integer totalCount;

  public List<AccountInfo> accountInfos() {
    return accountInfos;
  }

  public GetBlockedMembersResponse accountInfos(List<AccountInfo> accountInfos) {
    this.accountInfos = accountInfos;
    return this;
  }

  public GetBlockedMembersResponse accountInfos(AccountInfo... accountInfos) {
    this.accountInfos = Arrays.asList(accountInfos);
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public GetBlockedMembersResponse offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public GetBlockedMembersResponse limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer totalCount() {
    return totalCount;
  }

  public GetBlockedMembersResponse totalCount(Integer totalCount) {
    this.totalCount = totalCount;
    return this;
  }
}
