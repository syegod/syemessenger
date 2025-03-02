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
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class HistoryMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "sender_id")
  private Account sender;

  @OneToOne
  @JoinColumn(name = "room_id")
  private Room room;

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

  public Account sender() {
    return sender;
  }

  public HistoryMessage sender(Account sender) {
    this.sender = sender;
    return this;
  }

  public Room room() {
    return room;
  }

  public HistoryMessage room(Room room) {
    this.room = room;
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
