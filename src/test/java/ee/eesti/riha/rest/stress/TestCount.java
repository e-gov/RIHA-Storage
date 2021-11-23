package ee.eesti.riha.rest.stress;

import ee.eesti.riha.rest.dao.SecureApiGenericDAO;
import ee.eesti.riha.rest.model.Main_resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * Stress-tests count() performance.
 * 
 * @author A
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
//@Ignore
public class TestCount {
  
  private static final Integer LIMIT = 100;
  
  @Autowired
  SecureApiGenericDAO<Main_resource, Integer> secureDao;   // FIXME remove excess templating
  
  @Test
  @Timed(millis = 500)
  public void test() throws Exception {
    
    // fetch
    Integer count = secureDao.findCount(Main_resource.class, LIMIT, null,
        null, null);
    
    // assume we get LIMIT amount of results
    assertEquals(LIMIT, count);
    
  } // -test

}
