package io.syemessenger.api.room.repository;

import io.syemessenger.api.account.repository.Account;
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
@Table(name = "rooms")
public class Room {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;

  @OneToOne
  @JoinColumn(name = "owner_id")
  private Account owner;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Long id() {
    return id;
  }

  public Room id(Long id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public Room name(String name) {
    this.name = name;
    return this;
  }

  public String description() {
    return description;
  }

  public Room description(String description) {
    this.description = description;
    return this;
  }

  public Account owner() {
    return owner;
  }

  public Room owner(Account owner) {
    this.owner = owner;
    return this;
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  public Room createdAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public LocalDateTime updatedAt() {
    return updatedAt;
  }

  public Room updatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
