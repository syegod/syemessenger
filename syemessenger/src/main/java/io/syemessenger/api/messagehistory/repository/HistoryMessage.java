package io.syemessenger.api.messagehistory.repository;

import io.syemessenger.api.account.repository.Account;
import io.syemessenger.api.room.repository.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class HistoryMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sender_id")
  private Long senderId;

  @Column(name = "room_id")
  private Long roomId;

  private String message;

  @Column(name = "timestamp")
  private LocalDateTime timestamp;

  public Long id() {
    return id;
  }

  public HistoryMessage id(Long id) {
    this.id = id;
    return this;
  }

  public Long senderId() {
    return senderId;
  }

  public HistoryMessage senderId(Long senderId) {
    this.senderId = senderId;
    return this;
  }

  public Long roomId() {
    return roomId;
  }

  public HistoryMessage roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public String message() {
    return message;
  }

  public HistoryMessage message(String message) {
    this.message = message;
    return this;
  }

  public LocalDateTime timestamp() {
    return timestamp;
  }

  public HistoryMessage timestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }
}
