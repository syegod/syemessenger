package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.ServiceMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessageCodec {

  private final JsonMapper jsonMapper;
  private final Map<String, Class> qualifierMap;

  public MessageCodec(JsonMapper jsonMapper, Consumer<Map<String, Class>> consumer) {
    this.jsonMapper = jsonMapper;
    qualifierMap = new ConcurrentHashMap<>();
    consumer.accept(qualifierMap);
  }

  public Object decode(ServiceMessage message) {
    final var qualifier = message.qualifier();
    final var data = message.data();
    return jsonMapper.convertValue(data, qualifierMap.get(qualifier));
  }
}
