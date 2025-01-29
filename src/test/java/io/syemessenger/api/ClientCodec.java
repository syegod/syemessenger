package io.syemessenger.api;

import io.syemessenger.JsonMappers;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountViewInfo;
import io.syemessenger.api.room.RoomInfo;
import io.syemessenger.api.room.RoomInfoList;

public class ClientCodec extends MessageCodec {

  private static final ClientCodec INSTANCE = new ClientCodec();

  private ClientCodec() {
    super(
        JsonMappers.jsonMapper(),
        (map) -> {
          map.put("createAccount", AccountInfo.class);
          map.put("updateAccount", AccountInfo.class);
          map.put("login", Long.class);
          map.put("getSessionAccount", AccountInfo.class);
          map.put("showAccount", AccountViewInfo.class);

          map.put("createRoom", RoomInfo.class);
          map.put("updateRoom", RoomInfo.class);
          map.put("getRoom", RoomInfo.class);
          map.put("joinRoom", Long.class);
          map.put("leaveRoom", Long.class);
          map.put("removeRoomMembers", Long.class);
          map.put("blockRoomMembers", Long.class);
          map.put("unblockRoomMembers", Long.class);
          map.put("listRooms", RoomInfoList.class);

          map.put("error", ErrorData.class);
        });
  }

  public static ClientCodec getInstance() {
    return INSTANCE;
  }
}
