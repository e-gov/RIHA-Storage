package ee.eesti.riha.rest.stress;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.dao.SecureApiGenericDAO;
import ee.eesti.riha.rest.model.Main_resource;

/**
 * Stress-tests count() performance.
 * 
 * @author A
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
//@Ignore
public class TestCount {
  
  private static final Integer LIMIT = 100;
  
  @Autowired
  SecureApiGenericDAO<Main_resource, Integer> secureDao;   // FIXME remove excess templating
  
  @Autowired
  AuthInfo authInfo;
  
  @Test
  @Timed(millis = 500)
  public void test() throws Exception {
    
    // fetch
    Integer count = secureDao.findCount(Main_resource.class, LIMIT, null,
        null, null, authInfo);
    
    // assume we get LIMIT amount of results
    assertEquals(LIMIT, count);
    
  } // -test

}
