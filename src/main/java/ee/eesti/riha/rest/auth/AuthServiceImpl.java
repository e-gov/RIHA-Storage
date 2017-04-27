package ee.eesti.riha.rest.auth;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * Fake implementation of AuthService for testing.
 */
@Component
public class AuthServiceImpl implements AuthService {

  private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

  /**
   * Instantiates a new auth service impl.
   */
  public AuthServiceImpl() {
    LOG.info("AUTH constructor");
  }

  /**
   * Instantiates a new auth service impl.
   *
   * @param s the s
   */
  public AuthServiceImpl(String s) {
    LOG.info("AUTH constructor :: " + s);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.auth.AuthService#isValid(java.lang.String)
   */
  @Override
  public Object isValid(String token) {

    LOG.info("AUTH isValid called");
    if (StringUtils.isEmpty(token)) {
      return null;
    }
    // return new Object();
    return new AuthInfo("35512121234", "Jaan Mets", "70000833", "test");
  }

}
