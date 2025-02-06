package io.syemessenger.api.account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

  @Test
  void testHasher() {
    final var password = "test12345";
    final var signedPassword =
        PasswordHasher.signPassword(password, PasswordHasher.createSecretKey());

    final var verifyPassword = PasswordHasher.verifyPassword(password, signedPassword);
    assertTrue(verifyPassword);
  }

  @Test
  void testHasherFailed() {
    final var password = "test12345";
    final var signedPassword =
        PasswordHasher.signPassword(password, PasswordHasher.createSecretKey());

    final var verifyPassword = PasswordHasher.verifyPassword(password + "1", signedPassword);
    assertFalse(verifyPassword);
  }
}
