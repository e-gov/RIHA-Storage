package ee.eesti.riha.rest.integration;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.springframework.util.StringUtils;

import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.service.ApiClassicService;

public class IntegrationTestHelper {

  public static Integer addTestDataToDB(ApiClassicService client, String tableName, String jsonToUseForCreate) {

    Response response = client.create(jsonToUseForCreate, tableName);

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = null;
    try {
      jsonReturned = TestHelper.readStream(inpStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    String resultKey = StringUtils.deleteAny(jsonReturned, "[]");
    System.out.println(resultKey);
    return new Integer(resultKey.replace(".0", ""));

  }

  public static Response removeTestDataFromDB(ApiClassicService client, String tableName, Integer idForTestEntry) {

    return client.delete(tableName, idForTestEntry);

  }

}
