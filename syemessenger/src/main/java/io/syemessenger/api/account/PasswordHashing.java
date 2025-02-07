package io.syemessenger.api.account;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordHashing {

  public static String hash(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt(4));
  }

  public static boolean check(String password, String hash) {
    return BCrypt.checkpw(password, hash);
  }
}
