package io.syemessenger.environment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.syemessenger.api.OrderBy;
import io.syemessenger.api.OrderBy.Direction;
import io.syemessenger.api.ServiceMessage;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AssertionUtils {

  private AssertionUtils() {}

  public static <E> void assertCollections(
      Collection<E> expected, Collection<E> actual, BiConsumer<E, E> consumer) {
    if (expected == null) {
      assertNull(actual);
      return;
    }

    assertNotNull(actual);
    assertEquals(expected.size(), actual.size(), "size");

    final var expectedList = new ArrayList<>(expected);
    final var actualList = new ArrayList<>(actual);

    for (int i = 0; i < expectedList.size(); i++) {
      consumer.accept(expectedList.get(i), actualList.get(i));
    }
  }

  public static <T> Comparator<? super T> toComparator(OrderBy orderBy) {
    final var field = orderBy.field() != null ? orderBy.field() : "id";
    final var direction = orderBy.direction() != null ? orderBy.direction() : Direction.ASC;
    final var comparator = Comparator.comparing(t -> getFieldValue(field, t));
    return direction == Direction.ASC ? comparator : comparator.reversed();
  }

  public static <T, V> V getFieldValue(String name, T obj) {
    try {
      final Class<?> clazz = obj.getClass();
      final var field = clazz.getDeclaredField(name);
      field.setAccessible(true);
      //noinspection unchecked
      return (V) field.get(obj);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static String[] getFields(Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).toArray(String[]::new);
  }

  public static <T> T awaitUntil(Supplier<T> supplier, Duration timeout) {
    final var s = System.currentTimeMillis();
    while (true) {
      final var obj = supplier.get();
      if (obj != null) {
        return obj;
      }
      if (System.currentTimeMillis() - s >= timeout.toMillis()) {
        throw new RuntimeException("Timeout");
      }
      Thread.onSpinWait();
    }
  }

  public static <T> CompletableFuture awaitUntilAsync(Supplier<T> supplier, Duration timeout) {
    try {
      return CompletableFuture.supplyAsync(
          () -> {
            final var s = System.currentTimeMillis();
            while (true) {
              final var obj = supplier.get();
              if (obj != null) {
                return obj;
              }
              if (System.currentTimeMillis() - s >= timeout.toMillis()) {
                throw new RuntimeException("Timeout");
              }
              Thread.onSpinWait();
            }
          });
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static ServiceMessage getAwaited(CompletableFuture<ServiceMessage> future, Duration timeout) {
    try {
      return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Predicate<ServiceMessage> byCid(UUID cid) {
    return message -> cid.equals(message.cid());
  }

  public static Predicate<ServiceMessage> byQualifier(String qualifier) {
    return message -> qualifier.equals(message.qualifier());
  }
}
