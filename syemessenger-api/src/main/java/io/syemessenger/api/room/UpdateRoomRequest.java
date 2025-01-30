package io.syemessenger.api.room;

import java.util.StringJoiner;

public class UpdateRoomRequest {

  private Long roomId;
  private String description;

  public Long roomId() {
    return roomId;
  }

  public UpdateRoomRequest roomId(Long roomId) {
    this.roomId = roomId;
    return this;
  }

  public String description() {
    return description;
  }

  public UpdateRoomRequest description(String description) {
    this.description = description;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", UpdateRoomRequest.class.getSimpleName() + "[", "]")
        .add("roomId=" + roomId)
        .add("description='" + description + "'")
        .toString();
  }
}
