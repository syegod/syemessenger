package io.syemessenger.api;

import io.syemessenger.JsonMappers;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountViewInfo;
import io.syemessenger.api.account.GetRoomsResponse;
import io.syemessenger.api.room.GetBlockedMembersResponse;
import io.syemessenger.api.room.GetRoomMembersResponse;
import io.syemessenger.api.room.ListRoomsResponse;
import io.syemessenger.api.room.RoomInfo;

public class ClientCodec extends MessageCodec {

  private static final ClientCodec INSTANCE = new ClientCodec();

  private ClientCodec() {
    super(
        JsonMappers.jsonMapper(),
        (map) -> {
          map.put("v1/syemessenger/createAccount", AccountInfo.class);
          map.put("v1/syemessenger/updateAccount", AccountInfo.class);
          map.put("v1/syemessenger/login", Long.class);
          map.put("v1/syemessenger/getAccount", AccountInfo.class);
          map.put("v1/syemessenger/getRooms", GetRoomsResponse.class);

          map.put("v1/syemessenger/createRoom", RoomInfo.class);
          map.put("v1/syemessenger/updateRoom", RoomInfo.class);
          map.put("v1/syemessenger/getRoom", RoomInfo.class);
          map.put("v1/syemessenger/joinRoom", Long.class);
          map.put("v1/syemessenger/leaveRoom", Long.class);
          map.put("v1/syemessenger/getRoomMembers", GetRoomMembersResponse.class);
          map.put("v1/syemessenger/removeRoomMembers", Long.class);
          map.put("v1/syemessenger/blockRoomMembers", Long.class);
          map.put("v1/syemessenger/unblockRoomMembers", Long.class);
          map.put("v1/syemessenger/getBlockedMembers", GetBlockedMembersResponse.class);
          map.put("v1/syemessenger/listRooms", ListRoomsResponse.class);

          map.put("v1/syemessenger/error", ErrorData.class);
        });
  }

  public static ClientCodec getInstance() {
    return INSTANCE;
  }
}
