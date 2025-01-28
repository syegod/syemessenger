package io.syemessenger.api.account.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Long id() {
    return id;
  }

  public Account id(Long id) {
    this.id = id;
    return this;
  }

  public String username() {
    return username;
  }

  public Account username(String username) {
    this.username = username;
    return this;
  }

  public String email() {
    return email;
  }

  public Account email(String email) {
    this.email = email;
    return this;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public Account passwordHash(String passwordHash) {
    this.passwordHash = passwordHash;
    return this;
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  public Account createdAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public LocalDateTime updatedAt() {
    return updatedAt;
  }

  public Account updatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }
}
