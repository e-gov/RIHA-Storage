package ee.eesti.riha.rest.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.BaseModel;

// TODO: Auto-generated Javadoc
/**
 * Gather any supporting or helper methods here that are used to edit existing object/entry. Also some table specifics
 * related to updating entry in that table.
 *
 * @param <T> the generic type
 * @param <K> the key type
 */
// This class is currently never used
@Component
public class TableEntryUpdateLogic<T, K> {

  @Autowired
  ApiGenericDAO<T, K> genericDAO;

  /**
   * Updates json_content field on object of given id and of given table.
   *
   * @param json the json
   * @param id the id
   * @param classRepresentingTable class representing json content based table
   * @return the int
   * @throws RihaRestException the riha rest exception
   */
  public int updateJsonContentField(String json, Integer id, Class<T> classRepresentingTable) throws RihaRestException {

    int numOfChanged = 0;

    T obj = genericDAO.find(classRepresentingTable, id);
    // get current json_content
    BaseModel baseModel = (BaseModel) obj;

    JsonObject jsonContent = baseModel.getJson_content();
    // get json containing edit data
    JsonObject updateInfo = JsonHelper.getFromJson(json);
    // update json_content with edit data
    jsonContent = JsonHelper.updateJsonObjWithValuesFromAnotherJsonObj(jsonContent, updateInfo, classRepresentingTable);
    // update json_content in db
    // Main_resource mrCarryingEditInfo = new Main_resource();
    baseModel.setJson_content(jsonContent);

    numOfChanged = genericDAO.update(obj, id);

    return numOfChanged;
  }
}
