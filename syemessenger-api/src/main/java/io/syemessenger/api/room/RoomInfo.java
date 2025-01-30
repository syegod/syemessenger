package io.syemessenger.api.room;

import java.time.LocalDateTime;
import java.util.StringJoiner;

public class RoomInfo {

  private Long id;
  private String name;
  private String owner;
  private String description;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long id() {
    return id;
  }

  public RoomInfo id(Long id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public RoomInfo name(String name) {
    this.name = name;
    return this;
  }

  public String owner() {
    return owner;
  }

  public RoomInfo owner(String owner) {
    this.owner = owner;
    return this;
  }

  public String description() {
    return description;
  }

  public RoomInfo description(String description) {
    this.description = description;
    return this;
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  public RoomInfo createdAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public LocalDateTime updatedAt() {
    return updatedAt;
  }

  public RoomInfo updatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", RoomInfo.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("name='" + name + "'")
        .add("owner='" + owner + "'")
        .add("description='" + description + "'")
        .add("createdAt=" + createdAt)
        .add("updatedAt=" + updatedAt)
        .toString();
  }
}
