package io.syemessenger.environment;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class GlobalTestListener implements TestExecutionListener {

  private IntegrationEnvironment integrationEnvironment;

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    integrationEnvironment = new IntegrationEnvironment();
    integrationEnvironment.start();
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    if (integrationEnvironment != null) {
      integrationEnvironment.close();
    }
  }
}
