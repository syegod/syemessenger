package io.syemessenger.api;

import java.util.StringJoiner;

public class OrderBy {

  private String field;
  private Direction direction;

  public String field() {
    return field;
  }

  public OrderBy field(String field) {
    this.field = field;
    return this;
  }

  public Direction direction() {
    return direction;
  }

  public OrderBy direction(Direction direction) {
    this.direction = direction;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", OrderBy.class.getSimpleName() + "[", "]")
        .add("field='" + field + "'")
        .add("direction=" + direction)
        .toString();
  }

  public enum Direction {
    DESC,
    ASC;
  }
}
