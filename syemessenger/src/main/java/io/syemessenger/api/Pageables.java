package io.syemessenger.api;

import io.syemessenger.api.OrderBy.Direction;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
