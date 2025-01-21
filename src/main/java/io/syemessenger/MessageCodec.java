package io.syemessenger;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.syemessenger.api.ServiceMessage;
import java.util.Map;

public class MessageCodec {

  private final JsonMapper jsonMapper;
  private final Map<String, Class> qualifierMap;

  public MessageCodec(JsonMapper jsonMapper, Map<String, Class> map) {
    this.jsonMapper = jsonMapper;
    qualifierMap = map;
  }

  public Object decode(ServiceMessage message) {
    final var qualifier = message.qualifier();
    final var data = message.data();
    return jsonMapper.convertValue(data, qualifierMap.get(qualifier));
  }
}
