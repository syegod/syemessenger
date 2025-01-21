package io.syemessenger;

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
    applicationContext.registerBean(ServiceConfig.class, () -> config, bd -> {});
    applicationContext.refresh();
  }

  public void stop() {
    if (applicationContext != null) {
      applicationContext.close();
    }
  }
}
