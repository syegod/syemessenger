package io.syemessenger;

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

  public void subscribe(Long roomId, SessionContext sessionContext) {
    registry.compute(
        roomId,
        (k, list) -> {
          if (list == null) {
            list = new ArrayList<>();
          }
          if (!list.contains(sessionContext)) {
            list.add(sessionContext);
          }
          return list;
        });
  }

  public void unsubscribe(Long roomId, SessionContext sessionContext) {
    // TODO: implement
  }

  public void send(Long roomId, MessageInfo messageInfo) {
    // TODO: implement
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
