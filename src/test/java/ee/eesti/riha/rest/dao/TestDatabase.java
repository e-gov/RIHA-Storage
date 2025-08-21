package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebAppConfiguration
@SpringJUnitConfig(locations = "/test-applicationContext.xml")
public class TestDatabase {

  @Autowired
  ApiGenericDAO<Main_resource, Integer> genericDAO;

  @Autowired
  ApiGenericDAO<Data_object, Integer> dataObjectDAO;

  @Autowired
  ApiGenericDAO<Document, Integer> documentDAO;

  // if this test fails, then should investigate how json_content can become null
  @Test
  public void testNoItemsExistWithNullJsonContent() throws Exception {

    int countOfJsonContentNull = genericDAO.findCount(Main_resource.class, null, null,
        Arrays.asList(new FilterComponent("json_content", "isnull", null)), null);

    assertEquals(0, countOfJsonContentNull);
  }

  @Test
  public void testNoItemsExistWithNullJsonContentData_object() throws Exception {

    int countOfJsonContentNull = dataObjectDAO.findCount(Data_object.class, null, null,
        Arrays.asList(new FilterComponent("json_content", "isnull", null)), null);

    assertEquals(0, countOfJsonContentNull);
  }

  @Test
  public void testNoItemsExistWithNullJsonContentDocument() throws Exception {

    int countOfJsonContentNull = documentDAO.findCount(Document.class, null, null,
        Arrays.asList(new FilterComponent("json_content", "isnull", null)), null);

    assertEquals(0, countOfJsonContentNull);
  }

}
