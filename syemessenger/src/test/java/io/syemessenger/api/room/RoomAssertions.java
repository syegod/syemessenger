package io.syemessenger.api.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.LoginAccountRequest;

public class RoomAssertions {

  private RoomAssertions() {}

  public static RoomInfo createRoom(AccountInfo accountInfo) {
    try (final var client = new ClientSdk()) {
      client
          .accountSdk()
          .login(new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
      return client.roomSdk().createRoom(new CreateRoomRequest().name(randomAlphanumeric(8, 65)));
    }
  }

  public static void joinRoom(String name, final String username) {
    try (final var client = new ClientSdk()) {
      client.accountSdk().login(new LoginAccountRequest().username(username).password("test12345"));
      client.roomSdk().joinRoom(name);
    }
  }

  public static void assertRoom(RoomInfo expected, RoomInfo actual) {
    assertEquals(expected.id(), actual.id(), "actual.id");
    assertEquals(expected.name(), actual.name(), "actual.name");
    assertEquals(expected.description(), actual.description(), "actual.description");
    assertEquals(expected.createdAt(), actual.createdAt(), "actual.createdAt");
    assertEquals(expected.updatedAt(), actual.updatedAt(), "actual.updatedAt");
  }
}
