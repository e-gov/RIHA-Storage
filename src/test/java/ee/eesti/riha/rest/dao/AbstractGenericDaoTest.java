package ee.eesti.riha.rest.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.model.readonly.Kind;
import org.junit.After;
import org.junit.Before;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractGenericDaoTest {
    static final String INFOSYSTEM_KIND_NAME = "infosystem";
    static final Integer INFOSYSTEM_KIND_ID = 389;

    protected static final String EXAMPLE_NAME = "Resource 1X";
    protected static final String EXAMPLE_SHORT_NAME = "r1Y";
    protected static final int EXAMPLE_OLD_ID = 222222;

    @Resource
    protected ApiGenericDAO<Main_resource, Integer> mainResourceDao;

    @Resource
    protected ApiGenericDAO<Kind, Integer> kindDao;

    @Resource
    protected UtilitiesDAO<Main_resource> mainResourceUtilitiesDAO;

    // before every test this item will be created in db and would be accessible
    // for test; after test it would be deleted
    protected Main_resource mrAsPrimeTestEntry;
    // some tests require more than one item, these can be placed here inside
    // the test; items in this list will be deleted from db after test
    protected List<Main_resource> additionalMrTestEntries = new ArrayList<>();
    private Kind kind;

    @Before
    public void beforeTest() {
        kind = createKind(INFOSYSTEM_KIND_NAME, INFOSYSTEM_KIND_ID);
        kindDao.create(kind);
        mrAsPrimeTestEntry = createMain_resource();
        mainResourceDao.create(mrAsPrimeTestEntry);
    }

    @After
    public void afterTest() {
        kindDao.delete(kind);
        mainResourceDao.delete(mrAsPrimeTestEntry);
        mainResourceDao.delete(additionalMrTestEntries);
        additionalMrTestEntries.clear();
    }

    Main_resource createMain_resource() {
        Main_resource main_resource = new Main_resource();
        // required fields
        main_resource.setMain_resource_id(mainResourceUtilitiesDAO.getNextSeqValForPKForTable(Main_resource.class));
        main_resource.setUri("uri");
        main_resource.setName(EXAMPLE_NAME);
        main_resource.setVersion("1.1");
        main_resource.setKind(INFOSYSTEM_KIND_NAME);
        // TODO change, currently infosystem kind_id = 389
        main_resource.setKind_id(INFOSYSTEM_KIND_ID);

        main_resource.setCreator("test_creator");
        main_resource.setCreation_date(new Date());
        // not required fields
        main_resource.setShort_name(EXAMPLE_SHORT_NAME);
        main_resource.setOld_id(EXAMPLE_OLD_ID);
        main_resource.setField_name("testTEST01");

        // it is expected to have json_content
        main_resource.setJson_content(JsonHelper.getFromJson(JsonHelper.GSON.toJson(main_resource)));
        return main_resource;
    }

    Main_resource createMain_resource_withTestArray() {
        Main_resource mr = createMain_resource();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("asd");
        jsonArray.add("dfg");
        jsonArray.add("fgh");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("test_array", jsonArray);
        mr.setJson_content(jsonObject);
        return mr;
    }

    Main_resource createMain_resource_withJsonContent() {
        Main_resource mr = createMain_resource_withTestArray();
        mr.getJson_content().addProperty("not_null_value", "asd");
        mr.getJson_content().add("null_value", JsonNull.INSTANCE);
        return mr;
    }

    private Kind createKind(String name, Integer id) {
        Kind kind = new Kind();
        ReflectionTestUtils.setField(kind, "name", name);
        ReflectionTestUtils.setField(kind, "kind_id", id);

        return kind;
    }
}
