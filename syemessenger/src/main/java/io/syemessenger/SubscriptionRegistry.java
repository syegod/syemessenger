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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class SubscriptionRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRegistry.class);

  private final Map<Long, List<SessionContext>> registry = new ConcurrentHashMap<>();
  private final Map<SessionContext, Long> sessions = new ConcurrentHashMap<>();

  public Map<Long, List<SessionContext>> registry() {
    return registry;
  }

  public Map<SessionContext, Long> sessions() {
    return sessions;
  }

  public void subscribe(Long roomId, SessionContext sessionContext) {
    LOGGER.debug("Subscribe: session: {} on room: {}", sessionContext, roomId);
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
    LOGGER.debug("Unsubscribe: {} from room: {}", sessionContext, roomId);
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

  public void onRoomMessage(MessageInfo messageInfo) {
    final var list = registry.get(messageInfo.roomId());
    LOGGER.debug("Send to subscribers message: {}", messageInfo);
    if (list != null) {
      for (var sessionContext : list) {
        sessionContext.send(new ServiceMessage().qualifier("messages").data(messageInfo));
      }
    }
  }

  public void leaveRoom(Long roomId, Long accountId, Boolean isOwner) {
    LOGGER.debug(
        "Received leaveRoomEvent: roomId: {}, accountId: {}, isOwner: {}",
        roomId,
        accountId,
        isOwner);
    final var sessionContext = sessionContext(accountId);
    if (sessionContext == null) {
      return;
    }
    if (isOwner) {
      registry.remove(roomId);
      sessions.entrySet().removeIf(pair -> roomId.equals(pair.getValue()));
    } else {
      unsubscribe(sessionContext);
    }
  }

  public void removeMembers(Long roomId, List<Long> memberIds) {
    LOGGER.debug("Received removeMembersEvent: roomId: {}, memberIds: {}", roomId, memberIds);
    registry.computeIfPresent(
        roomId,
        (k, list) ->
            new ArrayList<>(
                list.stream().filter(e -> !memberIds.contains(e.accountId())).toList()));
    for (Long memberId : memberIds) {
      final var sessionContext = sessionContext(memberId);
      if (sessionContext != null) {
        sessions.remove(sessionContext, roomId);
      }
    }
  }

  public void blockMembers(Long roomId, List<Long> memberIds) {
    LOGGER.debug("Received blockMembersEvent: roomId: {}, memberIds: {}", roomId, memberIds);
    registry.computeIfPresent(
        roomId,
        (k, list) ->
            new ArrayList<>(
                list.stream().filter(e -> !memberIds.contains(e.accountId())).toList()));
    for (Long memberId : memberIds) {
      final var sessionContext = sessionContext(memberId);
      if (sessionContext != null) {
        sessions.remove(sessionContext, roomId);
      }
    }
  }

  public boolean isSubscribed(SessionContext sessionContext) {
    return sessions.containsKey(sessionContext);
  }

  public Long roomId(SessionContext sessionContext) {
    final var roomId = sessions.get(sessionContext);
    if (roomId == null) {
      throw new ServiceException(400, "Not subscribed");
    }
    return roomId;
  }

  private SessionContext sessionContext(Long accountId) {
    return sessions.keySet().stream()
        .filter(e -> accountId.equals(e.accountId()))
        .findFirst()
        .orElse(null);
  }
}
