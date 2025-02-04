package io.syemessenger.environment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.testcontainers.containers.PostgreSQLContainer;

public class GlobalTestListener implements TestExecutionListener {

  private IntegrationEnvironment integrationEnvironment;
  private PostgreSQLContainer postgresContainer;

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    integrationEnvironment = new IntegrationEnvironment();
    integrationEnvironment.start();
    postgresContainer = integrationEnvironment.getPostgresContainer();
  }

  @Override
  public void executionFinished(TestIdentifier testIdentifier,
      TestExecutionResult testExecutionResult) {
    // TODO: improve it
    if (testIdentifier.isTest()) {
      try (Connection connection = postgresContainer.createConnection("?currentSchema=syemessenger")) {
        String truncateQuery =
            "TRUNCATE TABLE accounts CASCADE; " +
                "TRUNCATE TABLE rooms CASCADE; " +
                "TRUNCATE TABLE messages CASCADE;";

        try (PreparedStatement statement = connection.prepareStatement(truncateQuery)) {
          statement.executeUpdate();
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    if (integrationEnvironment != null) {
      integrationEnvironment.close();
    }
  }
}
