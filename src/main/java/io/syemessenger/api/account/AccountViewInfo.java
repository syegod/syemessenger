package io.syemessenger.api.account;

import java.util.StringJoiner;

public class AccountViewInfo {

  private long id;
  private String username;

  public long id() {
    return id;
  }

  public AccountViewInfo id(long id) {
    this.id = id;
    return this;
  }

  public String username() {
    return username;
  }

  public AccountViewInfo username(String username) {
    this.username = username;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AccountViewInfo.class.getSimpleName() + "[", "]")
        .add("id=" + id)
        .add("username='" + username + "'")
        .toString();
  }
}
