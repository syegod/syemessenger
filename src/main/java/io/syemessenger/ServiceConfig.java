package io.syemessenger;

public class ServiceConfig {

  private int port = 8080;

  public int port() {
    return port;
  }

  public ServiceConfig port(int port) {
    this.port = port;
    return this;
  }
}
