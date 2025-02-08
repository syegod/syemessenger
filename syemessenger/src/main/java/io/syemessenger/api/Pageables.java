package io.syemessenger.api;

import io.syemessenger.api.OrderBy.Direction;
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
      } else {
        if (orderBy.field().equals("createdAt")) {
          orderBy.field("created_at");
        } else if (orderBy.field().equals("updatedAt")) {
          orderBy.field("updated_at");
        } else if (orderBy.field().equals("owner")) {
          orderBy.field("owner_id");
        }
      }
      if (orderBy.direction() == null) {
        orderBy.direction(Direction.ASC);
      }
    }

    final var direction =
        orderBy.direction() == Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;

    return new OffsetPageable(offset, limit, Sort.by(direction, orderBy.field()));
  }
}
