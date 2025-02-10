package io.syemessenger;

import io.syemessenger.annotations.RequestController;
import io.syemessenger.annotations.RequestHandler;
import io.syemessenger.websocket.SessionContext;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationContext;

@Named
public class ServiceRegistry {

  private final MessageCodec messageCodec;
  private final ApplicationContext applicationContext;

  private final Map<String, InvocationHandler> handlers = new ConcurrentHashMap<>();

  public ServiceRegistry(MessageCodec messageCodec, ApplicationContext applicationContext) {
    this.messageCodec = messageCodec;
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  private void init() {
    applicationContext
        .getBeansWithAnnotation(RequestController.class)
        .forEach((name, bean) -> processBean(bean));
  }

  private void processBean(Object bean) {
    final Class<?> beanClass = bean.getClass();
    final var declaredMethods = beanClass.getDeclaredMethods();
    for (var method : declaredMethods) {
      final var annotation = method.getAnnotation(RequestHandler.class);
      if (annotation != null) {
        final var qualifier = annotation.value();
        handlers.put(qualifier, new InvocationHandler(method, bean, messageCodec));
        registerDataType(qualifier, method, annotation.requestType());
      }
    }
  }

  public InvocationHandler lookup(String qualifier) {
    return handlers.get(qualifier);
  }

  private void registerDataType(String qualifier, Method method, Class<?> requestType) {
    final var parameterTypes = method.getParameterTypes();
    if (parameterTypes.length != 2) {
      throw new IllegalArgumentException("Wrong method arguments: " + method.getName());
    }
    if (!parameterTypes[0].isAssignableFrom(SessionContext.class)) {
      throw new IllegalArgumentException("Wrong method arguments: " + method.getName());
    }
    messageCodec.register(qualifier, requestType);
  }
}
