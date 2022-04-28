package ee.eesti.riha.rest;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Custom test runner to enable turning off token validation for integration tests.
 *
 */
public class MyTestRunner extends SpringJUnit4ClassRunner {

  public MyTestRunner(Class<?> clazz) throws InitializationError {
    super(clazz);
    System.out.println("MY_TEST_RUNNER created");
  }

  @Override
  public void run(RunNotifier notifier) {
    System.out.println("BEFORE RUN");
    MyTestRunListener listener = new MyTestRunListener();
    // have to start manually
    try {
      listener.testRunStarted(getDescription());
    } catch (Exception e) {
      e.printStackTrace();
    }
    notifier.addListener(listener);
    super.run(notifier);
  }

}
