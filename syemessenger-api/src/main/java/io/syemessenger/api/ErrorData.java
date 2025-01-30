package io.syemessenger.api;

import java.util.StringJoiner;

public class ErrorData {

  private final int errorCode;
  private final String errorMessage;

  public ErrorData() {
    errorCode = -1;
    errorMessage = null;
  }

  public ErrorData(int errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public int errorCode() {
    return errorCode;
  }

  public String errorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ErrorData.class.getSimpleName() + "[", "]")
        .add("errorCode=" + errorCode)
        .add("errorMessage='" + errorMessage + "'")
        .toString();
  }
}
