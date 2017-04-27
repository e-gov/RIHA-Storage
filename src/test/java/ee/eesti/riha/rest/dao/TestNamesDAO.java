package ee.eesti.riha.rest.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestNamesDAO {

  @Autowired
  NamesDAO namesDAO;

  @Test
  public void testGetOrganizationNames() {

    List<String> organizations = Arrays.asList(new String[] { "75039296", "70009646", "70001952" });

    Map<String, String> resultMap = namesDAO.getOrganizationNames(organizations);

    assertNotNull(resultMap);
    assertFalse(resultMap.isEmpty());
    System.out.println(resultMap);
  }

  @Test
  public void testGetPersonNames() {

    List<String> persons = Arrays.asList(new String[] { "48803190312", "38312280240", "47711110017" });

    Map<String, String> resultMap = namesDAO.getPersonNames(persons);

    assertNotNull(resultMap);
    assertFalse(resultMap.isEmpty());
    System.out.println(resultMap);
  }

  public void testGetClassifierNames() {

    List<String> classifierURIs = Arrays.asList(new String[] { "urn:fdc:riha.eesti.ee:2016:classifier:436069",
        "https://riha.eesti.ee/riha/onto/avalikhaldus/2009/r1", "http://www.sehke.ee/Sehke" });

    Map<String, String> resultMap = namesDAO.getUriNames(classifierURIs);

    assertNotNull(resultMap);
    assertFalse(resultMap.isEmpty());
    System.out.println(resultMap);
  }
}
