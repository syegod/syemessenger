package io.syemessenger;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonMappers {

  private JsonMappers() {}

  public static JsonMapper jsonMapper() {
        return JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
            .configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS, true)
            .serializationInclusion(Include.NON_NULL)
            .visibility(PropertyAccessor.FIELD, Visibility.ANY)
            .addModule(new JavaTimeModule())
            .build();
  }
}
