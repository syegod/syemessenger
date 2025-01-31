package io.syemessenger.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ErrorAssertions {

  private ErrorAssertions() {}

  public static void assertError(Exception ex, int errorCode, String errorMessage) {
    assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
    final var serviceException = (ServiceException) ex;
    assertEquals(errorCode, serviceException.errorCode());
    assertEquals(errorMessage, serviceException.getMessage());
  }
}
