package io.syemessenger.api;

import java.util.StringJoiner;
import java.util.UUID;

public class ServiceMessage implements Cloneable {

  private UUID cid;
  private String qualifier;
  private Object data;

  public String qualifier() {
    return qualifier;
  }

  public ServiceMessage qualifier(String qualifier) {
    this.qualifier = "v1/syemessenger/" + qualifier;
    return this;
  }

  public Object data() {
    return data;
  }

  public ServiceMessage data(Object data) {
    this.data = data;
    return this;
  }

  public UUID cid() {
    return cid;
  }

  public ServiceMessage cid(UUID cid) {
    this.cid = cid;
    return this;
  }

  @Override
  public ServiceMessage clone() {
    try {
      return (ServiceMessage) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ServiceMessage.class.getSimpleName() + "[", "]")
        .add("cid=" + cid)
        .add("qualifier='" + qualifier + "'")
        .add("data=" + data)
        .toString();
  }
}
