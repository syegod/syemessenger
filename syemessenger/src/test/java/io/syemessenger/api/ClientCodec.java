package io.syemessenger.api;

import io.syemessenger.JsonMappers;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.GetRoomsResponse;
import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.api.messagehistory.ListMessagesResponse;
import io.syemessenger.api.room.GetBlockedMembersResponse;
import io.syemessenger.api.room.GetRoomMembersResponse;
import io.syemessenger.api.room.ListRoomsResponse;
import io.syemessenger.api.room.RoomInfo;

public class ClientCodec extends MessageCodec {

  private static final ClientCodec INSTANCE = new ClientCodec();

  private ClientCodec() {
    super(JsonMappers.jsonMapper());
    register("v1/syemessenger/createAccount", AccountInfo.class);
    register("v1/syemessenger/updateAccount", AccountInfo.class);
    register("v1/syemessenger/login", Long.class);
    register("v1/syemessenger/getAccount", AccountInfo.class);
    register("v1/syemessenger/getRooms", GetRoomsResponse.class);
    //
    register("v1/syemessenger/createRoom", RoomInfo.class);
    register("v1/syemessenger/updateRoom", RoomInfo.class);
    register("v1/syemessenger/getRoom", RoomInfo.class);
    register("v1/syemessenger/joinRoom", Long.class);
    register("v1/syemessenger/leaveRoom", Long.class);
    register("v1/syemessenger/getRoomMembers", GetRoomMembersResponse.class);
    register("v1/syemessenger/removeRoomMembers", Long.class);
    register("v1/syemessenger/blockRoomMembers", Long.class);
    register("v1/syemessenger/unblockRoomMembers", Long.class);
    register("v1/syemessenger/getBlockedMembers", GetBlockedMembersResponse.class);
    register("v1/syemessenger/listRooms", ListRoomsResponse.class);
    //
    register("v1/syemessenger/subscribe", Long.class);
    register("v1/syemessenger/unsubscribe", Long.class);
    register("v1/syemessenger/send", Long.class);
    //
    register("v1/syemessenger/messages", MessageInfo.class);
    //
    register("v1/syemessenger/listMessages", ListMessagesResponse.class);
    //
    register("v1/syemessenger/error", ErrorData.class);
  }

  public static ClientCodec getInstance() {
    return INSTANCE;
  }
}
