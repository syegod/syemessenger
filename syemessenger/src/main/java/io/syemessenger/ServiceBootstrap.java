package io.syemessenger;

import io.syemessenger.kafka.KafkaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ServiceBootstrap implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBootstrap.class);

  private final ServiceConfig config;

  private AnnotationConfigApplicationContext applicationContext;

  public ServiceBootstrap(ServiceConfig config) {
    this.config = config;
  }

  public void start() {
    LOGGER.info("Starting with config: {}", config);
    applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.register(AppConfiguration.class);
    applicationContext.register(KafkaConfiguration.class);
    applicationContext.registerBean(ServiceConfig.class, () -> config, bd -> {});
    applicationContext.refresh();
  }

  public AnnotationConfigApplicationContext applicationContext() {
    return applicationContext;
  }

  @Override
  public void close() {
    if (applicationContext != null) {
      applicationContext.close();
    }
  }
}
