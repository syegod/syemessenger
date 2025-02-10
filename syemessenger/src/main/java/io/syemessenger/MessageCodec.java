package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.ServiceMessage;
import jakarta.inject.Named;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Named
public class MessageCodec {

  private final JsonMapper jsonMapper;

  private final Map<String, Class<?>> qualifierMap = new ConcurrentHashMap<>();

  public MessageCodec(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public Object decode(ServiceMessage message) {
    final var qualifier = message.qualifier();
    final var data = message.data();
    final var type = qualifierMap.get(qualifier);
    if (type == null) {
      throw new IllegalArgumentException("Cannot find qualifier: " + qualifier);
    }
    return jsonMapper.convertValue(data, type);
  }

  public void register(String qualifier, Class<?> dataType) {
    qualifierMap.put(qualifier, dataType);
  }
}
