package io.syemessenger.api.account;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PasswordHasher {

  private PasswordHasher() {}

  public static boolean verifyPassword(String password, String signedPassword) {
    final var key = signedPassword.substring(0, 32);

    return signedPassword.equals(signPassword(password, toByteArray(key)));
  }

  public static String signPassword(String password, byte[] key) {
    try {
      final var mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));

      final var digest = mac.doFinal(password.getBytes());

      return toHex(key) + toHex(digest);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] createSecretKey() {
    try {
      final var keyGenerator = KeyGenerator.getInstance("HmacSHA256");
      keyGenerator.init(128);

      return keyGenerator.generateKey().getEncoded();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static String toHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xFF & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  private static byte[] toByteArray(String hex) {
    int length = hex.length();
    byte[] byteArray = new byte[length / 2];
    for (int i = 0; i < length; i += 2) {
      byteArray[i / 2] =
          (byte)
              ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
    }
    return byteArray;
  }
}
