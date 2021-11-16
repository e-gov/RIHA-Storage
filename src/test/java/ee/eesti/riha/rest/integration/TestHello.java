package ee.eesti.riha.rest.integration;

import ee.eesti.riha.rest.MyTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestHello {

  @Test
  public void testHello() {
    System.out.println("hello");
  }
}
