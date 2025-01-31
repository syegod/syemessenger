package io.syemessenger.api.room;

import io.syemessenger.api.room.repository.Room;

public class RoomMappers {

  private RoomMappers() {}

  public static RoomInfo toRoomInfo(Room room) {
    return new RoomInfo()
        .id(room.id())
        .name(room.name())
        .owner(room.owner().username())
        .description(room.description())
        .createdAt(room.createdAt())
        .updatedAt(room.updatedAt());
  }
}
