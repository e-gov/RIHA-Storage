package ee.eesti.riha.rest.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestHello {

  @Test
  public void testHello() {
    System.out.println("hello");
  }
}
