package io.syemessenger.api;

public class ServiceException extends RuntimeException {

  private int errorCode;

  public ServiceException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public int errorCode() {
    return errorCode;
  }
}
