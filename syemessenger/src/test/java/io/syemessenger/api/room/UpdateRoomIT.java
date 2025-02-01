package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.LoginAccountRequest;
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

public class UpdateRoomIT {

  private static AccountInfo accountInfo1;
  private static AccountInfo accountInfo2;
  private static RoomInfo existingRoomInfo;

  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;

  @BeforeAll
  static void beforeAll() {
    accountInfo1 = createAccount();
    accountInfo2 = createAccount();
    existingRoomInfo = createRoom(accountInfo1);
  }

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk();
    accountSdk = clientSdk.api(AccountSdk.class);
    roomSdk = clientSdk.api(RoomSdk.class);
  }

  @AfterEach
  void afterEach() {
    CloseHelper.close(clientSdk);
  }

  @Test
  void testUpdateRoomNotLoggedIn() {
    try {
      final var description = randomAlphanumeric(20);
      roomSdk.updateRoom(
          new UpdateRoomRequest().roomId(existingRoomInfo.id()).description(description));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testUpdateRoomNotOwner() {
    try {
      accountSdk.login(
          new LoginAccountRequest().username(accountInfo2.username()).password("test12345"));
      roomSdk.updateRoom(
          new UpdateRoomRequest()
              .roomId(existingRoomInfo.id())
              .description(randomAlphanumeric(20)));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not allowed");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testUpdateRoomFailedMethodSource")
  void testUpdateRoomFailed(
      String test, UpdateRoomRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo1.username()).password("test12345"));
    try {
      roomSdk.updateRoom(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> testUpdateRoomFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "No room id",
            new UpdateRoomRequest().description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: roomId"),
        Arguments.of(
            "Wrong room id",
            new UpdateRoomRequest().roomId(100L).description(randomAlphanumeric(20)),
            404,
            "Room not found"),
        Arguments.of(
            "No description",
            new UpdateRoomRequest().roomId(existingRoomInfo.id()),
            400,
            "Missing or invalid: description"),
        Arguments.of(
            "Description too short",
            new UpdateRoomRequest()
                .roomId(existingRoomInfo.id())
                .description(randomAlphanumeric(5)),
            400,
            "Missing or invalid: description"),
        Arguments.of(
            "Description too long",
            new UpdateRoomRequest()
                .roomId(existingRoomInfo.id())
                .description(randomAlphanumeric(201)),
            400,
            "Missing or invalid: description"));
  }

  @Test
  void testUpdateRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo1.username()).password("test12345"));

    final var description = randomAlphanumeric(20);
    final var roomInfo =
        roomSdk.updateRoom(
            new UpdateRoomRequest().roomId(existingRoomInfo.id()).description(description));

    assertEquals(existingRoomInfo.id(), roomInfo.id(), "roomInfo.id: " + roomInfo.id());
    assertEquals(accountInfo1.username(), roomInfo.owner());
    assertEquals(description, roomInfo.description());
  }
}
