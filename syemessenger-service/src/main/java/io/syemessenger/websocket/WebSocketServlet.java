package io.syemessenger.websocket;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.account.AccountService;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.GetRoomsRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.api.account.UpdateAccountRequest;
import io.syemessenger.api.room.BlockMembersRequest;
import io.syemessenger.api.room.CreateRoomRequest;
import io.syemessenger.api.room.GetRoomMembersRequest;
import io.syemessenger.api.room.ListRoomsRequest;
import io.syemessenger.api.room.RemoveMembersRequest;
import io.syemessenger.api.room.RoomService;
import io.syemessenger.api.room.UnblockMembersRequest;
import io.syemessenger.api.room.UpdateRoomRequest;
import jakarta.inject.Named;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;

@Named
public class WebSocketServlet extends JettyWebSocketServlet {

  private final JsonMapper jsonMapper;
  private final MessageCodec messageCodec;
  private final AccountService accountService;
  private final RoomService roomService;

  public WebSocketServlet(
      JsonMapper jsonMapper, AccountService accountService, RoomService roomService) {
    this.jsonMapper = jsonMapper;
    this.accountService = accountService;
    this.roomService = roomService;
    this.messageCodec =
        new MessageCodec(
            jsonMapper,
            map -> {
              map.put("v1/syemessenger/createAccount", CreateAccountRequest.class);
              map.put("v1/syemessenger/updateAccount", UpdateAccountRequest.class);
              map.put("v1/syemessenger/showAccount", Long.class);
              map.put("v1/syemessenger/login", LoginAccountRequest.class);
              map.put("v1/syemessenger/getSessionAccount", Void.class);
              map.put("v1/syemessenger/getRooms", GetRoomsRequest.class);

              map.put("v1/syemessenger/createRoom", CreateRoomRequest.class);
              map.put("v1/syemessenger/updateRoom", UpdateRoomRequest.class);
              map.put("v1/syemessenger/getRoom", Long.class);
              map.put("v1/syemessenger/joinRoom", String.class);
              map.put("v1/syemessenger/leaveRoom", Long.class);
              map.put("v1/syemessenger/getRoomMembers", GetRoomMembersRequest.class);
              map.put("v1/syemessenger/removeRoomMembers", RemoveMembersRequest.class);
              map.put("v1/syemessenger/blockRoomMembers", BlockMembersRequest.class);
              map.put("v1/syemessenger/unblockRoomMembers", UnblockMembersRequest.class);
              map.put("v1/syemessenger/listRooms", ListRoomsRequest.class);
            });
  }

  @Override
  public void configure(JettyWebSocketServletFactory factory) {
    factory.addMapping(
        "/",
        (req, res) -> new WebSocketHandler(jsonMapper, messageCodec, accountService, roomService));
  }
}
