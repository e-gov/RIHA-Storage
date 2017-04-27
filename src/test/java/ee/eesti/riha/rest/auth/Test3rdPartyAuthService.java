package ee.eesti.riha.rest.auth;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.TestHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class Test3rdPartyAuthService {

  AuthServiceProvider authServiceProvider = AuthServiceProvider.getInstance();
  
  @Test
  public void testResponseHasNotChanged() throws IOException {

    AuthService authService = authServiceProvider.get();
    String sessionId = "123asd";
    InputStream is = (InputStream) authService.isValid(sessionId);
    JsonObject jsonObj = TestHelper.getObjectFromClient(is, JsonObject.class);
    
    assertNotNull(jsonObj);
    System.out.println(jsonObj);
    
    assertTrue(jsonObj.has("isikuKood"));
    assertTrue(jsonObj.has("roll"));
    assertTrue(jsonObj.has("asutus"));
    
    Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
    assertEquals(3, entries.size());
  }

}
