package io.syemessenger.api.room;

public interface RoomSdk {

  RoomInfo createRoom(CreateRoomRequest request);

  RoomInfo updateRoom(UpdateRoomRequest request);

  RoomInfo getRoom(Long id);

  Long joinRoom(String name);

  Long leaveRoom(Long id);

  Long removeRoomMembers(RemoveMembersRequest request);

  Long blockRoomMembers(BlockMembersRequest request);

  Long unblockRoomMembers(UnblockMembersRequest request);

  RoomInfoList listRooms(ListRoomsRequest request);
}
