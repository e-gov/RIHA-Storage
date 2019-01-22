package ee.eesti.riha.rest.dao;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Main_resource;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestApiGenericDAOMain_resource extends AbstractGenericDaoTest {
  @Test
  public void testFindAll() throws RihaRestException {
    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, null, null, null, null);

    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
  }

  @Test
  public void testFindById() {
    Main_resource found = mainResourceDao.find(Main_resource.class, mrAsPrimeTestEntry.getMain_resource_id());
    assertNotNull(found);
  }

  @Test
  public void testFindCountAll() {
    Integer rowCount = mainResourceDao.findCount(Main_resource.class);
    assertNotNull(rowCount);
    // at least test data must exist
    assertTrue(rowCount >= 1);
  }

  @Test
  public void testFindCountFiltered() throws Exception {
    int limit = 25;
    Integer rowCount = mainResourceDao.findCount(Main_resource.class, limit, 0, null, null);
    assertNotNull(rowCount);
    // at least test data must exist
    assertTrue(rowCount >= 1);
    assertTrue(rowCount <= limit);
  }

  @Test
  public void testFindCountFilteredLimit0() throws Exception {
    Integer limit = 0;
    Integer rowCount = mainResourceDao.findCount(Main_resource.class, limit, 0, null, null);
    assertNotNull(rowCount);
    assertEquals(limit, rowCount);
  }

  @Ignore("Currently not using default limit ")
  @Test
  public void testFindCountFilteredDefaultLimit() throws Exception {
    Integer limit = 1234567;
    Integer rowCount = mainResourceDao.findCount(Main_resource.class, limit, null, null, null);
    assertNotNull(rowCount);
    assertEquals((Integer) Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED, rowCount);
  }

  @Test
  public void testFindFilteredSingle() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();

    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    System.out.println("FILTER");
    FilterComponent fc = new FilterComponent("test_array", "?&", "[\"dfg\"]");
    List<FilterComponent> fcList = Collections.singletonList(fc);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, fcList, null);

    // at least test data must exist
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
    assertEquals(mr.getJson_content(), main_resources.get(0).getJson_content());

  }

  @Test
  public void testFindFilteredMultiple() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();

    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    System.out.println("FILTER");
    FilterComponent fc = new FilterComponent("test_array", "?&", "[\"asd\",\"dfg\"]");
    List<FilterComponent> fcList = Collections.singletonList(fc);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, fcList, null);

    // at least test data must exist
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
    assertEquals(mr.getJson_content(), main_resources.get(0).getJson_content());

  }

  @Test
  public void testFindFilteredAll() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();

    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    System.out.println("FILTER");
    FilterComponent fc = new FilterComponent("test_array", "?&", "[\"asd\",\"dfg\",\"fgh\"]");
    List<FilterComponent> fcList = Collections.singletonList(fc);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, fcList, null);

    // at least test data must exist
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
    assertEquals(mr.getJson_content(), main_resources.get(0).getJson_content());

  }

  @Test
  public void testFindFiltered_wrongFilter_thenEmpty() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();

    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    System.out.println("FILTER");
    FilterComponent fc = new FilterComponent("test_array", "?&", "[\"yyy\"]");
    List<FilterComponent> fcList = Collections.singletonList(fc);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, fcList, null);

    assertNotNull(main_resources);
    assertTrue(main_resources.isEmpty());
  }

  @Test
  public void testFindFiltered_oneWrongFilter_thenEmpty() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();

    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    System.out.println("FILTER");
    FilterComponent fc = new FilterComponent("test_array", "?&", "[\"asd\",\"yyy\"]");
    List<FilterComponent> fcList = Collections.singletonList(fc);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, fcList, null);

    assertNotNull(main_resources);
    assertTrue(main_resources.isEmpty());

  }

  @Test
  public void testFindFiltered_notArray_thenError() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();

    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    System.out.println("FILTER");
    FilterComponent fc = new FilterComponent("test_array", "?&", "\"asd\"");
    List<FilterComponent> fcList = Collections.singletonList(fc);

    RihaRestException rre = null;
    RihaRestError error = null;
    try {
      mainResourceDao.find(Main_resource.class, 1, 0, fcList, null);
    } catch (RihaRestException e) {
      rre = e;
    }

    assertNotNull(rre);
    error = (RihaRestError) rre.getError();
    assertNotNull(error);
    assertEquals(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY, error.getErrcode());
    assertEquals(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY_MSG, error.getErrmsg());

  }

  @Test
  public void testFindFiltered_isNull() throws Exception {

    // testing
    FilterComponent fc = new FilterComponent("parent_uri", "isnull", null);

    List<Main_resource> main_resources = mainResourceDao
            .find(Main_resource.class, 1, 0, Collections.singletonList(fc), "-creation_date");

    // at least test data must exist
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
    assertNull(main_resources.get(0).getParent_uri());
    assertEquals(mrAsPrimeTestEntry.callGetId(), main_resources.get(0).callGetId());

  }

  @Test
  public void testFindFiltered_isNotNull() throws Exception {
    // testing
    FilterComponent fc = new FilterComponent("kind", "isnotnull", null);

    List<Main_resource> main_resources = mainResourceDao
            .find(Main_resource.class, 1, 0, Collections.singletonList(fc), "-creation_date");

    // at least test data must exist
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
    assertNotNull(main_resources.get(0).getKind());
    assertEquals(mrAsPrimeTestEntry.callGetId(), main_resources.get(0).callGetId());

  }

  @Test
  public void testFindFilteredOverJson_isNull() throws Exception {

    // create test data
    Main_resource mr = createMain_resource_withJsonContent();
    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    FilterComponent fc = new FilterComponent("null_value", "isnull", null);
    FilterComponent fc2 = new FilterComponent("not_null_value", "isnotnull", null);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, Arrays.asList(fc, fc2),
            "-creation_date");

    // at least test data must exist
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
    assertEquals(JsonNull.class, main_resources.get(0).getJson_content().get("null_value").getClass());
    assertEquals(mr.callGetId(), main_resources.get(0).callGetId());

  }

  @Test
  public void testFindFilteredOverJson_isNotNull() throws Exception {

    // create test data
    Main_resource mr = createMain_resource_withJsonContent();
    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    FilterComponent fc = new FilterComponent("not_null_value", "isnotnull", null);

    List<Main_resource> main_resources = mainResourceDao
            .find(Main_resource.class, 1, 0, Collections.singletonList(fc), "-creation_date");

    // at least test data must exist
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
    assertNotNull(main_resources.get(0).getJson_content().get("not_null_value"));
    assertEquals(mr.callGetId(), main_resources.get(0).callGetId());

  }

  @Test
  public void testFindFiltered_invalidKind_thenEmpty() throws Exception {
    // create test data
    String badKind = "asdasdgfsdaf_some_kind";
    Main_resource mr = createMain_resource_withTestArray();
    mr.setKind(badKind);
    mr.setKind_id(null);
    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    System.out.println("FILTER");
    FilterComponent fc = new FilterComponent("kind", "=", badKind);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, Collections.singletonList(fc), null);
    assertNotNull(main_resources);
    assertEquals(0, main_resources.size());
  }

  @Test
  public void testFindFiltered_findByKindName_ifIdNotGiven() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();
    mr.setKind(INFOSYSTEM_KIND_NAME);
    mr.setKind_id(null);
    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // testing
    FilterComponent fc = new FilterComponent("kind", "=", INFOSYSTEM_KIND_NAME);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, Collections.singletonList(fc), null);
    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
  }

  @Test
  public void testFindFiltered_kindIsNull_thenEmpty() throws Exception {
    // create test data
    Main_resource mr = createMain_resource_withTestArray();
    mr.setKind(null);
    mr.setKind_id(INFOSYSTEM_KIND_ID);
    additionalMrTestEntries.add(mr);
    mainResourceDao.create(additionalMrTestEntries);

    // kind replacing in SqlFilter.constructSqlFilter() with kind_id hides kind field
    // but no row in Kind table has name NULL
    FilterComponent fc = new FilterComponent("kind", "isnull", null);

    List<Main_resource> main_resources = mainResourceDao.find(Main_resource.class, 1, 0, Collections.singletonList(fc), null);
    assertNotNull(main_resources);
    assertTrue(main_resources.isEmpty());
  }

  @Test
  public void testCreate() {
    Main_resource main_resource = createMain_resource();
    List<Integer> createdIds = mainResourceDao.create(main_resource);
    additionalMrTestEntries.add(main_resource);

    assertNotNull(createdIds);
    assertFalse(createdIds.isEmpty());

    assertEquals(main_resource.getMain_resource_id().intValue(), createdIds.get(0).intValue());

  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateNull() {

    Main_resource main_resource = null;
    mainResourceDao.create(main_resource);
  }

  //  @Test(expected = ConstraintViolationException.class)
  @Test(expected = DataIntegrityViolationException.class)
  public void testCreateSame() {

    Main_resource main_resource = mrAsPrimeTestEntry;
    // try to use entry again that contains id already existing in db
    try {
      mainResourceDao.create(main_resource);
    } catch (ConstraintViolationException e) {
      throw e;
    }
  }

  @Test
  public void testCreateList() {

    // create 3 items
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());

    List<Main_resource> main_resources = Arrays.asList(additionalMrTestEntries.get(0),
            additionalMrTestEntries.get(1), additionalMrTestEntries.get(2));

    List<Integer> createdIds = mainResourceDao.create(main_resources);

    List<Integer> originalIds = new ArrayList<Integer>();
    for (Main_resource mr : additionalMrTestEntries) {
      originalIds.add(mr.getMain_resource_id());
    }
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());

    assertNotNull(createdIds);
    assertFalse(createdIds.isEmpty());
    assertEquals(main_resources.size(), createdIds.size());
    for (Integer origId : originalIds) {
      assertTrue(createdIds.contains(origId));
    }

  }

  @Test
  public void testCreateListSame() {

    additionalMrTestEntries.add(createMain_resource());
    // List<Main_resource> main_resources = Arrays.asList(
    // new Main_resource[] { additionalMrTestEntries.get(0),
    // additionalMrTestEntries.get(0), additionalMrTestEntries.get(0) });

    List<Integer> createdIds = mainResourceDao.create(additionalMrTestEntries);

    assertNotNull(createdIds);
    assertFalse(createdIds.isEmpty());
    assertEquals(1, createdIds.size());
  }

  @Test
  public void testUpdate() throws RihaRestException {
    mrAsPrimeTestEntry.setName("TESTjunit:: changedName1");
    int numOfChanged = mainResourceDao.update(mrAsPrimeTestEntry, mrAsPrimeTestEntry.getMain_resource_id());
    assertEquals(1, numOfChanged);
  }

  @Test
  public void testUpdateBeforeCreate() throws RihaRestException {
    Main_resource main_resource = createMain_resource();

    int numOfChanged = mainResourceDao.update(main_resource, main_resource.getMain_resource_id());
    assertEquals(0, numOfChanged);
  }

  //
  // // TODO Cleaning up files that were not related to riha-rest
  // // @Test
  // // public void testUpdateJson() {
  // // Main_resource main_resource = createRandomMain_resource();
  // // main_resource.setName("TEST:: changedNameJson");
  // // JsonParser parser = new JsonParser();
  // // JsonObject jsonObject =
  // // parser.parse(InfosystemDAOImpl.EXAMPLE_JSON).getAsJsonObject();
  // // main_resource.setJson_content(jsonObject);
  // // // create test resource
  // // mainResourceDao.create(main_resource);
  // // // update
  // // int numOfChanged = mainResourceDao.update(main_resource,
  // // main_resource.getMain_resource_id());
  // // assertEquals(1, numOfChanged);
  // //
  // // // delete
  // // mainResourceDao.delete(main_resource);
  // // }
  //

  @Test
  public void testUpdateNull() throws RihaRestException {

    Main_resource main_resource = null;

    int numOfChanged = mainResourceDao.update(main_resource, mrAsPrimeTestEntry.getMain_resource_id());
    assertEquals(0, numOfChanged);

  }

  @Test
  public void testUpdateMultiple() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
          IllegalAccessException, RihaRestException {

    // create 3 items
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());

    int count = 0;
    for (Main_resource main_resource : additionalMrTestEntries) {
      main_resource.setName("TEST_NAME_A_" + count++);
    }

    // create test resources
    mainResourceDao.create(additionalMrTestEntries);

    for (Main_resource main_resource : additionalMrTestEntries) {
      main_resource.setShort_name("TEST_S_" + count);
    }

    // update
    int numOfChanged = mainResourceDao.update(additionalMrTestEntries, Finals.NAME);
    assertEquals(3, numOfChanged);

  }

  @Test
  public void testCopyNotNull() throws Exception {

    Main_resource newWithSmallDifference = createMain_resource();
    newWithSmallDifference.setName("Totally different name");
    newWithSmallDifference.setShort_name(null);

    // assertFalse(mrAsPrimeTestEntry.getName().equals(newWithSmallDifference.getName()));

    System.out.println(mrAsPrimeTestEntry.getName());
    ApiGenericDAOImpl.copyNotNullValues(mrAsPrimeTestEntry, newWithSmallDifference);
    System.out.println(mrAsPrimeTestEntry.getName());

    assertTrue(mrAsPrimeTestEntry.getName().equals(newWithSmallDifference.getName()));
    assertNotNull(mrAsPrimeTestEntry.getShort_name());

  }

  @Test
  public void testCopyNullValuesFromJson() throws Exception {

    Main_resource mrWithJsonContent = createMain_resource();
    JsonObject jsonContent = (JsonObject) JsonHelper.GSON.toJsonTree(mrWithJsonContent, Main_resource.class);
    mrWithJsonContent.setJson_content(jsonContent);

    JsonObject updateJson = new JsonObject();
    updateJson.add("field_name", JsonNull.INSTANCE);
    updateJson.add("kind", JsonNull.INSTANCE);

    ApiGenericDAOImpl.copyNullValuesFromJson(mrWithJsonContent, updateJson);

    assertNull(mrWithJsonContent.getField_name());
    // kind can't be updated
    assertNotNull(mrWithJsonContent.getKind());

  }

  @Test
  public void testDelete() {

    int numOfDeleted = mainResourceDao.delete(Main_resource.class, mrAsPrimeTestEntry.getMain_resource_id());
    assertEquals(1, numOfDeleted);

    Main_resource deletedModel = mainResourceDao.find(Main_resource.class, mrAsPrimeTestEntry.getMain_resource_id());
    assertNull(deletedModel);

  }

  @Test
  public void testDeleteNotExisting() {

    Main_resource main_resource = createMain_resource();
    int numOfDeleted = mainResourceDao.delete(Main_resource.class, main_resource.getMain_resource_id());
    assertEquals(0, numOfDeleted);

  }

  // // @Test
  // // public void testTempClearAll() {
  // // // delete all where organization=organization
  // // mainResourceDao.delete(Main_resource.class.getSimpleName(),
  // // Finals.ORGANIZATION, new String[] { "organization" });
  // //
  // // List<Main_resource> main_resources = mainResourceDao
  // // .findAll(Main_resource.class);
  // // System.out.println(main_resources);
  // // System.out.println("SIZE: " + main_resources.size());
  // // assertNotNull(main_resources);
  // // assertTrue(main_resources.isEmpty());
  // // }

  @Test
  public void testDeleteNull() {

    // provide is as null
    int numOfDeleted = mainResourceDao.delete(Main_resource.class, null);
    // id existing, but provide object as null
    int numOfDeleted2 = mainResourceDao.delete(null, mrAsPrimeTestEntry.getMain_resource_id());
    assertEquals(0, numOfDeleted);
    assertEquals(0, numOfDeleted2);

  }

  @Test
  public void testDeleteMultiple() {
    // create 3 items
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());
    additionalMrTestEntries.add(createMain_resource());

    String creator = "TEST_DELETE_X_:junit:01";
    List<String> fieldValues = new ArrayList<>();
    int count = 0;
    for (Main_resource main_resource : additionalMrTestEntries) {
      String newCreator = creator + count++;
      main_resource.setCreator(newCreator);
      fieldValues.add(newCreator);
    }

    String[] values = fieldValues.toArray(new String[0]);

    // create test resource
    mainResourceDao.create(additionalMrTestEntries);

    // delete test resource
    int numOfDeleted = mainResourceDao.delete(Main_resource.class.getSimpleName(), Finals.MAIN_RESOURCE_CREATOR, values);
    // check delete worked
    assertEquals(additionalMrTestEntries.size(), numOfDeleted);
  }
}
