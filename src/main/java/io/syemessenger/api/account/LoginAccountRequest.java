package io.syemessenger.api.account;

import java.util.StringJoiner;

public class LoginAccountRequest {

  private String username;
  private String email;
  private String password;

  public String username() {
    return username;
  }

  public LoginAccountRequest username(String username) {
    this.username = username;
    return this;
  }

  public String email() {
    return email;
  }

  public LoginAccountRequest email(String email) {
    this.email = email;
    return this;
  }

  public String password() {
    return password;
  }

  public LoginAccountRequest password(String password) {
    this.password = password;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", LoginAccountRequest.class.getSimpleName() + "[", "]")
        .add("username='" + username + "'")
        .add("email='" + email + "'")
        .add("password='" + password + "'")
        .toString();
  }
}
