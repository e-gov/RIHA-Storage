package ee.eesti.riha.rest.logic.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class QueryHolderTest {

  PathHolder pathHolder;

  @Test
  public void testQueryHolderJson() throws Exception {

    String json = "{\"op\":\"post\", \"path\": \"db/infosystem\"}";
    QueryHolder queryHolder = JsonHelper.GSON.fromJson(json, QueryHolder.class);
    assertNotNull(queryHolder);

  }

}
