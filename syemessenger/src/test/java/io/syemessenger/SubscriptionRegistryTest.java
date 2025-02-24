package io.syemessenger;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.websocket.SessionContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionRegistryTest {

  private SubscriptionRegistry subscriptionRegistry;
  private Map<Long, List<SessionContext>> registry;
  private Map<SessionContext, Long> sessions;

  @BeforeEach
  void beforeEach() {
    subscriptionRegistry = new SubscriptionRegistry();
    registry = subscriptionRegistry.registry();
    sessions = subscriptionRegistry.sessions();
  }

  @Test
  void testSubscribeSingle() {
    SessionContext sessionContext = mock(SessionContext.class);
    when(sessionContext.accountId()).thenReturn(Long.MAX_VALUE);
    subscriptionRegistry.subscribe(Long.MAX_VALUE, sessionContext);

    assertEquals(1, registry.size());
    assertEquals(1, sessions.size());
    assertTrue(registry.containsKey(Long.MAX_VALUE));
    assertTrue(sessions.containsValue(Long.MAX_VALUE));
  }

  @Test
  void testSubscribeManyRooms() {
    final var n = 25;

    for (long i = 0; i < n; i++) {
      SessionContext sessionContext = mock(SessionContext.class);
      when(sessionContext.accountId()).thenReturn(i);
      subscriptionRegistry.subscribe(i, sessionContext);

      assertTrue(sessions.containsKey(sessionContext));
      assertTrue(registry.get(i).contains(sessionContext));
    }

    assertEquals(n, registry.size());
    assertEquals(n, sessions.size());
  }

  @Test
  void testSubscribeManyRoomsManySessions() {
    final var n = 25;

    for (long i = 0; i < n; i++) {
      for (long j = 0; j < n; j++) {
        SessionContext sessionContext = mock(SessionContext.class);
        when(sessionContext.accountId()).thenReturn(j);
        subscriptionRegistry.subscribe(i, sessionContext);

        assertTrue(sessions.containsKey(sessionContext));
        assertTrue(registry.get(i).contains(sessionContext));
      }
    }

    assertEquals(n, registry.size());
    assertEquals(n * n, sessions.size());
  }

  @Test
  void testSubscribeOneRoomManySessions() {
    final var n = 25;
    final var roomId = 1L;

    for (long i = 0; i < n; i++) {
      SessionContext sessionContext = mock(SessionContext.class);
      when(sessionContext.accountId()).thenReturn(i);
      subscriptionRegistry.subscribe(roomId, sessionContext);

      assertTrue(sessions.containsKey(sessionContext));
      assertTrue(registry.get(roomId).contains(sessionContext));
    }

    assertEquals(1, registry.size());
    assertEquals(n, sessions.size());
  }

  @Test
  void testUnsubscribe() {
    final var n = 25;
    final var roomId = 1L;

    for (long i = 0; i < n; i++) {
      SessionContext sessionContext = mock(SessionContext.class);
      when(sessionContext.accountId()).thenReturn(i);
      subscriptionRegistry.subscribe(roomId, sessionContext);
      subscriptionRegistry.unsubscribe(sessionContext);

      assertFalse(sessions.containsKey(sessionContext));
      assertFalse(registry.get(roomId).contains(sessionContext));
    }

    assertEquals(1, registry.size());
    assertEquals(0, sessions.size());
  }

  @Test
  void testOnRoomMessage() {
    final var n = 25;
    final var roomId = 1L;

    for (long i = 0; i < n; i++) {
      SessionContext sessionContext = mock(SessionContext.class);
      when(sessionContext.accountId()).thenReturn(i);
      subscriptionRegistry.subscribe(roomId, sessionContext);

      final var messageInfo =
          new MessageInfo().message(randomAlphanumeric(10)).roomId(roomId).senderId(i);
      subscriptionRegistry.onRoomMessage(messageInfo);

      Mockito.verify(sessionContext)
          .send(new ServiceMessage().qualifier("messages").data(messageInfo));
    }
  }

  @Test
  void testLeaveRoomNotOwner() {
    final var id = 1L;

    SessionContext sessionContext = mock(SessionContext.class);
    when(sessionContext.accountId()).thenReturn(id);

    subscriptionRegistry.subscribe(id, sessionContext);

    subscriptionRegistry.leaveRoom(id, id, false);

    assertEquals(1, registry.size());
    assertEquals(0, sessions.size());
    assertFalse(registry.get(id).contains(sessionContext));
  }

  @Test
  void testLeaveRoomOwner() {
    final var n = 25;
    final var roomId = 1L;

    for (long i = 0; i < n; i++) {
      SessionContext sessionContext = mock(SessionContext.class);
      when(sessionContext.accountId()).thenReturn(i);
      subscriptionRegistry.subscribe(roomId, sessionContext);
    }

    subscriptionRegistry.leaveRoom(roomId, 1L, true);

    assertEquals(0, registry.size());
    assertEquals(0, sessions.size());
  }
}
