package ee.eesti.riha.rest.dao;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.model.readonly.Asutus;
import ee.eesti.riha.rest.model.readonly.Isik;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestApiGenericDAONames {

  @Autowired
  ApiGenericDAO<Isik, Integer> genericDAO;

  @Autowired
  ApiGenericDAO<Asutus, Integer> asutusDAO;

  @Test
  public void testFindNoFilter() throws Exception {
    List<Isik> isiks = genericDAO.find(Isik.class, 10, null, null, null);
    assertNotNull(isiks);
    assertFalse(isiks.isEmpty());
    assertNotNull(isiks.get(0).getEesnimi());
    System.out.println(isiks);
    System.out.println(isiks.get(0).getKood());
    System.out.println(isiks.get(0).getEesnimi());
    System.out.println(isiks.get(0).getEesnimi());
  }

  @Test
  public void testFindById() throws Exception {
    Isik isik = genericDAO.find(Isik.class, 1001109);
    assertNotNull(isik);
    assertEquals(1001109, (int) isik.getI_id());
  }

  @Test
  public void testFindFilter() throws Exception {
    FilterComponent fc = new FilterComponent("eesnimi", "ilike", "%a%");
    FilterComponent fc2 = new FilterComponent("perenimi", "ilike", "%s%");
    FilterComponent fc3 = new FilterComponent("kood", "isnotnull", null);
    List<Isik> isiks = genericDAO.find(Isik.class, 10, null, Arrays.asList(fc, fc2, fc3), null);
    assertNotNull(isiks);
    assertFalse(isiks.isEmpty());

    // assert filter worked
    assertTrue(isiks.get(0).getEesnimi().contains("a"));
    assertTrue(isiks.get(0).getPerenimi().contains("s"));
    assertNotNull(isiks.get(0).getKood());

  }

  @Test
  public void testFindFilter_Asutus() throws Exception {
    FilterComponent fc = new FilterComponent("nimetus", "ilike", "%a%");
    FilterComponent fc2 = new FilterComponent("registrikood", "ilike", "%7%");
    List<Asutus> asutuses = asutusDAO.find(Asutus.class, 10, null, Arrays.asList(fc, fc2), null);
    assertNotNull(asutuses);
    assertFalse(asutuses.isEmpty());

    // assert filter worked
    assertTrue(asutuses.get(0).getNimetus().contains("a"));
    assertTrue(asutuses.get(0).getRegistrikood().contains("7"));

  }

}
