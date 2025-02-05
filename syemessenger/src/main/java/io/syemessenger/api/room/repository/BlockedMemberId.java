package io.syemessenger.api.room.repository;

import java.io.Serializable;

public class BlockedMemberId implements Serializable {

  private Long roomId;

  private Long accountId;

  public Long roomId() {
    return roomId;
  }

  public BlockedMemberId roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public Long accountId() {
    return accountId;
  }

  public BlockedMemberId accountId(Long accountId) {
    this.accountId = accountId;
    return this;
  }
}
