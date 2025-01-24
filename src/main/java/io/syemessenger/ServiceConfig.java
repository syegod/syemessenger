package io.syemessenger;

import java.util.StringJoiner;

public class ServiceConfig {

  private int port = 8080;
  private String dbUrl;
  private String dbUser;
  private String dbPassword;

  public int port() {
    return port;
  }

  public ServiceConfig port(int port) {
    this.port = port;
    return this;
  }

  public String dbUrl() {
    return dbUrl;
  }

  public ServiceConfig dbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
    return this;
  }

  public String dbUser() {
    return dbUser;
  }

  public ServiceConfig dbUser(String dbUser) {
    this.dbUser = dbUser;
    return this;
  }

  public String dbPassword() {
    return dbPassword;
  }

  public ServiceConfig dbPassword(String dbPassword) {
    this.dbPassword = dbPassword;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ServiceConfig.class.getSimpleName() + "[", "]")
        .add("port=" + port)
        .add("dbUrl='" + dbUrl + "'")
        .add("dbUser='" + dbUser + "'")
        .add("dbPassword='" + dbPassword + "'")
        .toString();
  }
}
