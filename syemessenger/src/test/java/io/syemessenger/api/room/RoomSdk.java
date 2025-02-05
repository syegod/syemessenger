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

  ListRoomsResponse listRooms(ListRoomsRequest request);

  GetRoomMembersResponse getRoomMembers(GetRoomMembersRequest request);

  GetBlockedMembersResponse getBlockedMembers(GetBlockedMembersRequest request);
}
