package io.syemessenger.api.room;

import java.util.StringJoiner;

public class CreateRoomRequest {

  private String name;
  private String description;

  public String name() {
    return name;
  }

  public CreateRoomRequest name(String name) {
    this.name = name;
    return this;
  }

  public String description() {
    return description;
  }

  public CreateRoomRequest description(String description) {
    this.description = description;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CreateRoomRequest.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .add("description='" + description + "'")
        .toString();
  }
}
