package ee.eesti.riha.rest.logic;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.model.readonly.Role_right;
import static ee.eesti.riha.rest.logic.Role_rightCalculator.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestRole_rightCalculator {

  @Autowired
  Role_rightCalculator roleCalculator;

  @Autowired
  KindRepository kindRepository;

  int infosystemId;
  AuthInfo authInfo = new AuthInfo("Test_kood", "test_asutus", "ROLL_RIHA_ADMINISTRAATOR", "testToken123");

  @Before
  public void beforeTest() {
    infosystemId = kindRepository.getByName("infosystem").getKind_id();
  }

  @Test
  public void testDoCalculateRights_accessRestrictionTooHigh_thenNoRights() {
    int accessRestriction = 11;
    Role_right calculated = roleCalculator.calculate(authInfo, infosystemId, accessRestriction);

    assertNotNull(calculated);
    System.out.println(calculated);

    // no rights because accessRestriction too high
    assertEquals(NONE, (int) calculated.getCreate());
    assertEquals(NONE, (int) calculated.getRead());
    assertEquals(NONE, (int) calculated.getUpdate());
    assertEquals(NONE, (int) calculated.getDelete());
  }

  @Test
  public void testDoCalculateRights_default() {

    int accessRestriction = 0;
    Role_right calculated = roleCalculator.calculate(AuthInfo.DEFAULT, infosystemId, accessRestriction);

    assertNotNull(calculated);
    System.out.println(calculated);

    // no rights because accessRestriction too high
    assertEquals(NONE, (int) calculated.getCreate());
    assertEquals(ALL, (int) calculated.getRead());
    assertEquals(NONE, (int) calculated.getUpdate());
    assertEquals(NONE, (int) calculated.getDelete());
  }

  @Test
  public void testDoCalculateRights_authenticated() {
    AuthInfo testAuthInfo = new AuthInfo("TEST", "TEST", "AUTHENTICATED", "testToken123");
    int accessRestriction = 0;
    Role_right calculated = roleCalculator.calculate(testAuthInfo, infosystemId, accessRestriction);

    assertNotNull(calculated);
    System.out.println(calculated);

    // no rights because accessRestriction too high
    assertEquals(NONE, (int) calculated.getCreate());
    assertEquals(ALL, (int) calculated.getRead());
    assertEquals(NONE, (int) calculated.getUpdate());
    assertEquals(NONE, (int) calculated.getDelete());
  }

  @Test
  public void testDoCalculateRights_rihaAdmin() {
    int accessRestriction = 0;
    Role_right calculated = roleCalculator.calculate(authInfo, infosystemId, accessRestriction);

    assertNotNull(calculated);
    System.out.println(calculated);

    // no rights because accessRestriction too high
    assertEquals(ALL, (int) calculated.getCreate());
    assertEquals(ALL, (int) calculated.getRead());
    assertEquals(ALL, (int) calculated.getUpdate());
    assertEquals(ALL, (int) calculated.getDelete());
  }

}
