package io.syemessenger.api.room.outbox.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_room_events")
public class OutboxRoomEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "room_id")
  private Long roomId;

  private byte[] data;

  public Long id() {
    return id;
  }

  public OutboxRoomEvent id(Long id) {
    this.id = id;
    return this;
  }

  public Long roomId() {
    return roomId;
  }

  public OutboxRoomEvent roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public byte[] data() {
    return data;
  }

  public OutboxRoomEvent data(byte[] data) {
    this.data = data;
    return this;
  }
}
