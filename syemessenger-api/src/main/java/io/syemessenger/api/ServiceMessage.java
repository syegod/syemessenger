package io.syemessenger.api;

import java.util.Objects;
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
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;
    ServiceMessage message = (ServiceMessage) o;
    return Objects.equals(cid, message.cid) && Objects.equals(qualifier, message.qualifier)
        && Objects.equals(data, message.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cid, qualifier, data);
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
