package ee.eesti.riha.rest.logic.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QueryHolderTest {

  PathHolder pathHolder;

  @Test
  public void testQueryHolderJson() throws Exception {

    String json = "{\"op\":\"post\", \"path\": \"db/infosystem\"}";
    QueryHolder queryHolder = JsonHelper.GSON.fromJson(json, QueryHolder.class);
    assertNotNull(queryHolder);

  }

}
