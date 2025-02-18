package io.syemessenger.api;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class Flux<T> implements AutoCloseable {

  private final UUID cid;
  private final Queue<T> queue;
  private final Map<UUID, BlockingQueue<Object>> map;

  public Flux(UUID cid, Queue<T> queue, Map<UUID, BlockingQueue<Object>> map) {
    this.cid = cid;
    this.queue = queue;
    this.map = map;
  }

  public Queue<T> queue() {
    return queue;
  }

  @Override
  public void close() {
    map.remove(cid);
  }
}
