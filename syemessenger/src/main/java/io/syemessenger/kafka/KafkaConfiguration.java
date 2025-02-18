package io.syemessenger.kafka;

import io.syemessenger.ServiceConfig;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteBufferDeserializer;
import org.apache.kafka.common.serialization.ByteBufferSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaConfiguration {

  @Bean
  public KafkaTemplate<String, ByteBuffer> kafkaTemplate(
      ProducerFactory<String, ByteBuffer> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public ProducerFactory<String, ByteBuffer> producerFactory(ServiceConfig serviceConfig) {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serviceConfig.kafkaBootstrapServers());
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteBufferSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public ConsumerFactory<String, ByteBuffer> consumerFactory(ServiceConfig serviceConfig) {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serviceConfig.kafkaBootstrapServers());
    config.put(ConsumerConfig.GROUP_ID_CONFIG, serviceConfig.kafkaConsumerGroup());
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteBufferDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ByteBuffer> kafkaListenerContainerFactory(
      ServiceConfig serviceConfig) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, ByteBuffer>();
    factory.setConsumerFactory(consumerFactory(serviceConfig));
    return factory;
  }
}
