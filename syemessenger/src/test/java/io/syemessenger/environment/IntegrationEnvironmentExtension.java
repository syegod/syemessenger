package io.syemessenger.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class IntegrationEnvironmentExtension
    implements BeforeAllCallback, ParameterResolver, CloseableResource {

  private static final IntegrationEnvironment environment = new IntegrationEnvironment();
  private static final Map<Class<?>, Supplier<?>> PARAMETERS_TO_RESOLVE = new HashMap<>();

  @Override
  public void beforeAll(ExtensionContext context) {
    context
        .getRoot()
        .getStore(Namespace.GLOBAL)
        .getOrComputeIfAbsent(
            this.getClass(),
            key -> {
              environment.start();
              return this;
            });

    PARAMETERS_TO_RESOLVE.put(IntegrationEnvironment.class, () -> environment);
    PARAMETERS_TO_RESOLVE.put(DataSource.class, () -> environment.getBean(DataSource.class));
  }

  @Override
  public void close() throws Throwable {
    CloseHelper.close(environment);
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    return PARAMETERS_TO_RESOLVE.keySet().stream().anyMatch(type::isAssignableFrom);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    return PARAMETERS_TO_RESOLVE.get(type).get();
  }
}
