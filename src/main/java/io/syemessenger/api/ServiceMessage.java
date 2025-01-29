package io.syemessenger.api;

import java.util.StringJoiner;

public class ServiceMessage {

  private String qualifier;
  private Object data;

  public String qualifier() {
    return qualifier;
  }

  public ServiceMessage qualifier(String qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  public Object data() {
    return data;
  }

  public ServiceMessage data(Object data) {
    this.data = data;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ServiceMessage.class.getSimpleName() + "[", "]")
        .add("qualifier='" + qualifier + "'")
        .add("data=" + data)
        .toString();
  }
}
