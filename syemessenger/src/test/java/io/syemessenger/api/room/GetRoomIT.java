package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.assertRoom;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.IntegrationEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetRoomIT {

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
  void testGetRoomNotLoggedIn() {
    try {
      roomSdk.getRoom(existingRoomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testGetRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    final var roomInfo = roomSdk.getRoom(existingRoomInfo.id());
    assertRoom(existingRoomInfo, roomInfo);
  }
}
