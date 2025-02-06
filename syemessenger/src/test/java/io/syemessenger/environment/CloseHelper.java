package io.syemessenger.environment;

public class CloseHelper {

  private CloseHelper() {}

  public static void close(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
