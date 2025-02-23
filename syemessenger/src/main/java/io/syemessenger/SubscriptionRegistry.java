package io.syemessenger;

import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Named
public class SubscriptionRegistry {

  private final Map<Long, List<SessionContext>> registry = new ConcurrentHashMap<>();
  private final Map<SessionContext, Long> sessions = new ConcurrentHashMap<>();

  public void subscribe(Long roomId, SessionContext sessionContext) {
    sessions.compute(
        sessionContext,
        (k, currentRoomId) -> {
          List<SessionContext> list;
          if (currentRoomId != null) {
            list = registry.get(currentRoomId);
            list.remove(sessionContext);
          } else {
            list = registry.computeIfAbsent(roomId, l -> new ArrayList<>());
          }
          list.add(sessionContext);
          return roomId;
        });
  }

  public Long unsubscribe(SessionContext sessionContext) {
    final var roomId = roomId(sessionContext);
    sessions.compute(
        sessionContext,
        (k, currentRoomId) -> {
          if (currentRoomId != null) {
            registry.get(currentRoomId).remove(sessionContext);
          }
          return null;
        });
    return roomId;
  }

  public Long roomId(SessionContext sessionContext) {
    final var roomId = sessions.get(sessionContext);
    if (roomId == null) {
      throw new ServiceException(400, "Not subscribed");
    }
    return roomId;
  }

  public void onRoomMessage(MessageInfo messageInfo) {
    final var list = registry.get(messageInfo.roomId());
    if (list != null) {
      for (var sessionContext : list) {
        sessionContext.send(new ServiceMessage().qualifier("messages").data(messageInfo));
      }
    }
  }

  public void leaveRoom(Long roomId, Long accountInfo, Boolean isOwner) {
    // TODO: implement
  }

  public void removeMembers(Long roomId, List<Long> memberIds) {
    // TODO: implement
  }

  public void blockMembers(Long roomId, List<Long> memberIds) {
    // TODO: implement
  }
}
