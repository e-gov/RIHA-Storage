package ee.eesti.riha.rest.auth;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestTokenStoreImpl {

  TokenStoreImpl tokenStore = new TokenStoreImpl();
  String token = "asdasd";
  String personCode = "123123";
  AuthInfo testAuthInfo = new AuthInfo(personCode, null, null, null);

  static final long TEST_TIME_OUT_IN_MS = 200L;
  long sleep = TEST_TIME_OUT_IN_MS + 10;
  List<Map<String, String>> timeOutStorage = TokenStoreImpl.getTimeOutStorage();

  @Before
  public void beforeTest() {
    tokenStore.clear();
    TokenStoreImpl.setTest(false);
    TokenStoreImpl.setTimeOutInMS(TEST_TIME_OUT_IN_MS);
  }

  @Test
  public void testAddToken() {
    tokenStore.addToken(token, testAuthInfo);
    AuthInfo authInfo = tokenStore.tokenExists(token);

    assertNotNull(authInfo);
    assertNull(authInfo.getOrg_code());
    assertEquals(personCode, authInfo.getUser_code());
  }

  @Test
  public void testTokenExists_withTest() {
    TokenStoreImpl.setTest(true);
    tokenStore.addToken(token, testAuthInfo);
    AuthInfo authInfo = tokenStore.tokenExists(token);

    assertNotNull(authInfo);
    // TokenStoreIMpl testAuthInfo is returned
    assertNotNull(authInfo.getOrg_code());
    assertNotEquals(personCode, authInfo.getUser_code());

  }

  @Test
  public void testTokenExists_withTimeout() throws InterruptedException {
    System.out.println("BEFOER FAIL: " + TokenStoreImpl.getTokenStorage());

    tokenStore.addToken(token, testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 0, 0);
    Thread.sleep(sleep);

    tokenStore.addToken(token + "xxx", testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 1, 0);
    Thread.sleep(sleep);

    tokenStore.addToken(token + "yyy", testAuthInfo);
    assertStorageSizes(timeOutStorage, 0, 1, 1);
    Thread.sleep(sleep);

    tokenStore.addToken(token + "zzz", testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 0, 1);
    Thread.sleep(sleep);

    tokenStore.addToken(token + "ppp", testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 1, 0);
    Thread.sleep(sleep);

    tokenStore.addToken(token + "SSS", testAuthInfo);
    assertStorageSizes(timeOutStorage, 0, 1, 1);
    tokenStore.printTimeoutStorage();

    assertNull(tokenStore.tokenExists(token));

    assertNotNull(tokenStore.tokenExists(token + "ppp"));
    assertNotNull(tokenStore.tokenExists(token + "SSS"));

    assertEquals(2, TokenStoreImpl.getTokenStorage().size());
  }

  @Test
  public void testTokenExists_withTimeout_useMultipleTimes() throws InterruptedException {
    tokenStore.addToken(token, testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 0, 0);
    Thread.sleep(sleep);

    tokenStore.addToken(token + "xxx", testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 1, 0);
    Thread.sleep(sleep);

    assertNotNull(tokenStore.tokenExists(token));

    tokenStore.addToken(token + "yyy", testAuthInfo);
    assertStorageSizes(timeOutStorage, 0, 2, 1);
    Thread.sleep(sleep);

    assertNotNull(tokenStore.tokenExists(token));
    assertNotNull(tokenStore.tokenExists(token + "xxx"));

    tokenStore.addToken(token + "zzz", testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 0, 3);
    Thread.sleep(sleep);

    assertNotNull(tokenStore.tokenExists(token));
    assertNotNull(tokenStore.tokenExists(token + "xxx"));
    assertNotNull(tokenStore.tokenExists(token + "yyy"));
    assertNotNull(tokenStore.tokenExists(token + "zzz"));

    assertEquals(4, TokenStoreImpl.getTokenStorage().size());
  }

  @Test
  public void testTokenExists_withTimeout_addOldToken() throws InterruptedException {
    tokenStore.addToken(token, testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 0, 0);
    Thread.sleep(sleep);

    tokenStore.addToken(token + "xxx", testAuthInfo);
    assertStorageSizes(timeOutStorage, 1, 1, 0);
    Thread.sleep(sleep);

    tokenStore.addToken(token, testAuthInfo);
    assertStorageSizes(timeOutStorage, 0, 1, 1);
    Thread.sleep(sleep);

    assertTrue(TokenStoreImpl.getTokenStorage().containsKey(token));

    assertEquals(2, TokenStoreImpl.getTokenStorage().size());
  }

  // @Test
  // public void testTokenExists_withTimeout_existsOldToken() throws InterruptedException {
  // tokenStore.addToken(token, testAuthInfo);
  // assertStorageSizes(timeOutStorage, 1, 0, 0);
  // Thread.sleep(sleep);
  //
  // tokenStore.addToken(token + "xxx", testAuthInfo);
  // assertStorageSizes(timeOutStorage, 1, 1, 0);
  // Thread.sleep(sleep);
  //
  // assertNull(tokenStore.tokenExists(token));
  // assertStorageSizes(timeOutStorage, 0, 1, 1);
  // Thread.sleep(sleep);
  //
  // assertTrue(TokenStoreImpl.getTokenStorage().containsKey(token));
  //
  // assertEquals(2, TokenStoreImpl.getTokenStorage().size());
  // }

  private void assertStorageSizes(List<Map<String, String>> timeOutStorage, int a, int b, int c) {
    assertEquals(a, timeOutStorage.get(0).size());
    assertEquals(b, timeOutStorage.get(1).size());
    assertEquals(c, timeOutStorage.get(2).size());
  }

  @Test
  public void testNext() {
    assertEquals(1, TokenStoreImpl.next(0));
    assertEquals(2, TokenStoreImpl.next(1));
    assertEquals(0, TokenStoreImpl.next(2));
  }

  @Test
  public void testPrevious() {
    assertEquals(2, TokenStoreImpl.previous(0));
    assertEquals(0, TokenStoreImpl.previous(1));
    assertEquals(1, TokenStoreImpl.previous(2));
  }

}
