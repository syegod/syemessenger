package io.syemessenger.api;

public class ServiceException extends RuntimeException {

  private int errorCode;

  public int errorCode() {
    return errorCode;
  }
}
