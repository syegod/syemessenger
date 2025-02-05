package io.syemessenger.api.room.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "blocked_users")
@IdClass(BlockedMemberId.class)
public class BlockedMember {

  @Id
  @Column(name = "room_id")
  private Long roomId;

  @Id
  @Column(name = "account_id")
  private Long accountId;

  public Long roomId() {
    return roomId;
  }

  public BlockedMember roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public Long accountId() {
    return accountId;
  }

  public BlockedMember accountId(Long accountId) {
    this.accountId = accountId;
    return this;
  }
}
