package io.syemessenger.api.room;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceMessage;

public class RoomSdkImpl implements RoomSdk {

  private final ClientSdk clientSdk;

  public RoomSdkImpl(ClientSdk clientSdk) {
    this.clientSdk = clientSdk;
  }

  @Override
  public RoomInfo createRoom(CreateRoomRequest request) {
    final var message = new ServiceMessage().qualifier("createRoom").data(request);
    clientSdk.sendText(message);
    return (RoomInfo) clientSdk.pollResponse();
  }

  @Override
  public RoomInfo updateRoom(UpdateRoomRequest request) {
    final var message = new ServiceMessage().qualifier("updateRoom").data(request);
    clientSdk.sendText(message);
    return (RoomInfo) clientSdk.pollResponse();
  }

  @Override
  public RoomInfo getRoom(Long id) {
    final var message = new ServiceMessage().qualifier("getRoom").data(id);
    clientSdk.sendText(message);
    return (RoomInfo) clientSdk.pollResponse();
  }

  @Override
  public Long joinRoom(String name) {
    final var message = new ServiceMessage().qualifier("joinRoom").data(name);
    clientSdk.sendText(message);
    return (Long) clientSdk.pollResponse();
  }

  @Override
  public Long leaveRoom(Long id) {
    final var message = new ServiceMessage().qualifier("leaveRoom").data(id);
    clientSdk.sendText(message);
    return (Long) clientSdk.pollResponse();
  }

  @Override
  public Long removeRoomMembers(RemoveMembersRequest request) {
    final var message = new ServiceMessage().qualifier("removeMembers").data(request);
    clientSdk.sendText(message);
    return (Long) clientSdk.pollResponse();
  }

  @Override
  public Long blockRoomMembers(BlockMembersRequest request) {
    final var message = new ServiceMessage().qualifier("blockMembers").data(request);
    clientSdk.sendText(message);
    return (Long) clientSdk.pollResponse();
  }

  @Override
  public Long unblockRoomMembers(UnblockMembersRequest request) {
    final var message = new ServiceMessage().qualifier("unblockMembers").data(request);
    clientSdk.sendText(message);
    return (Long) clientSdk.pollResponse();
  }

  @Override
  public RoomInfoList listRooms(ListRoomsRequest request) {
    final var message = new ServiceMessage().qualifier("unblockMembers").data(request);
    clientSdk.sendText(message);
    return (RoomInfoList) clientSdk.pollResponse();
  }

  @Override
  public RoomMemberList getRoomMembers(GetRoomMembersRequest request) {
    final var message = new ServiceMessage().qualifier("getRoomMembers").data(request);
    clientSdk.sendText(message);
    return (RoomMemberList) clientSdk.pollResponse();
  }
}
