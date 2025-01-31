package io.syemessenger.api;

import io.syemessenger.api.OrderBy.Direction;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class Pageables {

  private Pageables() {}

  public static Pageable toPageable(Integer offset, Integer limit, OrderBy orderBy) {
    if (offset == null) {
      offset = 0;
    }

    if (limit == null) {
      limit = 50;
    }

    if (orderBy == null) {
      orderBy = new OrderBy().field("id").direction(Direction.ASC);
    } else {
      if (orderBy.field() == null) {
        orderBy.field("id");
      }
      if (orderBy.direction() == null) {
        orderBy.direction(Direction.ASC);
      }
    }

    final var direction =
        orderBy.direction() == Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;

    return PageRequest.of(offset, limit, Sort.by(direction, orderBy.field()));
  }
}
