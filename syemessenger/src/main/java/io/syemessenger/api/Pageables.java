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

    return PageRequest.of(offset, limit, Sort.by(direction, orderBy.field()));
  }

  public static OrderBy toSort(OrderBy orderBy) {
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

    return new OrderBy().field(orderBy.field()).direction(orderBy.direction());
  }

  public static Long getTotalCount(Tuple tuple) {
    return tuple != null ? (Long) tuple.get("total_count") : 0;
  }

  public static <T> T getEntity(Tuple tuple, Class<T> clazz) {
    if (tuple == null) {
      return null;
    }

    try {
      final var instance = clazz.getDeclaredConstructor().newInstance();

      final var fields = clazz.getDeclaredFields();
      for (var f : fields) {
        final var columnAnnotation = f.getAnnotation(Column.class);
        final var convertAnnotation = f.getAnnotation(Convert.class);
        f.setAccessible(true);
        if (columnAnnotation != null) {
          var value = tuple.get(columnAnnotation.name());
          if (convertAnnotation != null) {
            final var converter =
                (AttributeConverter)
                    convertAnnotation.converter().getDeclaredConstructor().newInstance();
            f.set(instance, converter.convertToEntityAttribute(value));
          } else {
            f.set(instance, value);
          }
        } else {
          final var value = tuple.get(f.getName());
          if (convertAnnotation != null) {
            final var converter =
                (AttributeConverter)
                    convertAnnotation.converter().getDeclaredConstructor().newInstance();
            f.set(instance, converter.convertToEntityAttribute(value));
          } else {
            f.set(instance, value);
          }
        }
      }

      return instance;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
