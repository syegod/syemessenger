package io.syemessenger;

public class ServiceRunner {

  public static void main(String[] args) throws InterruptedException {
    try (final var serviceBootstrap = new ServiceBootstrap(ServiceConfig.fromSystemProperties())) {
      serviceBootstrap.start();
      Thread.currentThread().join();
    }
  }
}
