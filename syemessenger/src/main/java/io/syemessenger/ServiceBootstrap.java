package io.syemessenger;

import io.syemessenger.kafka.KafkaConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ServiceBootstrap {

  private final ServiceConfig config;

  private AnnotationConfigApplicationContext applicationContext;

  public ServiceBootstrap(ServiceConfig config) {
    this.config = config;
  }

  public void start() {
    applicationContext = new AnnotationConfigApplicationContext();
    applicationContext.register(AppConfiguration.class);
    applicationContext.register(KafkaConfiguration.class);
    applicationContext.registerBean(ServiceConfig.class, () -> config, bd -> {});
    applicationContext.refresh();
  }

  public AnnotationConfigApplicationContext applicationContext() {
    return applicationContext;
  }

  public void close() {
    if (applicationContext != null) {
      applicationContext.close();
    }
  }
}
