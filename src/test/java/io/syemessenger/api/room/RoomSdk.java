package io.syemessenger.api.room;

public interface RoomSdk {

  RoomInfo createRoom(CreateRoomRequest request);

  RoomInfo updateRoom(UpdateRoomRequest request);

  RoomInfo getRoom(Long id);

  Long joinRoom(String name);

  Long leaveRoom(Long id);

  Long removeMembers(RemoveMembersRequest request);

  Long blockMembers(BlockMembersRequest request);

  Long unblockMembers(UnblockMembersRequest request);

  RoomInfoList listRooms(ListRoomsRequest request);
}
