package io.syemessenger.api;

import java.util.StringJoiner;

public class ServiceException extends RuntimeException {

  private int errorCode;

  public ServiceException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public int errorCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
        .add("errorCode=" + errorCode)
        .add("errorMessage='" + getMessage() + "'")
        .toString();
  }
}
