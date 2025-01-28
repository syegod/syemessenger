package io.syemessenger.api.account;

import java.util.StringJoiner;

public class UpdateAccountRequest {

  private Long id;
  private String username;
  private String email;
  private String password;

  public Long id() {
    return id;
  }

  public UpdateAccountRequest id(Long id) {
    this.id = id;
    return this;
  }

  public String username() {
    return username;
  }

  public UpdateAccountRequest username(String username) {
    this.username = username;
    return this;
  }

  public String email() {
    return email;
  }

  public UpdateAccountRequest email(String email) {
    this.email = email;
    return this;
  }

  public String password() {
    return password;
  }

  public UpdateAccountRequest password(String password) {
    this.password = password;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", UpdateAccountRequest.class.getSimpleName() + "[", "]")
        .add("username='" + username + "'")
        .add("email='" + email + "'")
        .add("password='" + password + "'")
        .toString();
  }
}
