package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.assertRoom;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.room.RoomInfo;
import io.syemessenger.api.room.RoomSdk;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.IntegrationEnvironment;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GetRoomsIT {

  private static IntegrationEnvironment environment;

  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;
  private AccountInfo existingAccountInfo;
  private RoomInfo existingRoomInfo;

  @BeforeAll
  static void beforeAll() {
    environment = new IntegrationEnvironment();
    environment.start();
  }

  @AfterAll
  static void afterAll() {
    CloseHelper.close(environment);
  }

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk();
    accountSdk = clientSdk.api(AccountSdk.class);
    roomSdk = clientSdk.api(RoomSdk.class);
    existingAccountInfo = createAccount();
    existingRoomInfo = createRoom(existingAccountInfo);
  }

  @AfterEach
  void afterEach() {
    CloseHelper.close(clientSdk);
  }

  @Test
  void testGetRoomsNotLoggedIn() {
    fail("Implement");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testGetRoomsFailedMethodSource")
  void testGetRoomsFailed(
      String test, GetRoomsRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    try {
      accountSdk.getRooms(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> testGetRoomsFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "Offset is negative",
            new GetRoomsRequest().offset(-50),
            400,
            "Missing or invalid: offset"),
        Arguments.of(
            "Limit is negative",
            new GetRoomsRequest().limit(-50),
            400,
            "Missing or invalid: limit"),
        Arguments.of(
            "Limit is over than max",
            new GetRoomsRequest().limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @Test
  void testGetRooms() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    final var rooms = accountSdk.getRooms(new GetRoomsRequest());
    assertEquals(1, rooms.roomInfos().size());
    assertRoom(existingRoomInfo, rooms.roomInfos().getFirst());
  }

  @Test
  void testGetRoomsEmpty() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    final var rooms = accountSdk.getRooms(new GetRoomsRequest());
    assertEquals(0, rooms.roomInfos().size());
  }
}
