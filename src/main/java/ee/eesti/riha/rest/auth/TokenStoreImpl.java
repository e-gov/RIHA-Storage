package ee.eesti.riha.rest.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.util.PropsReader;

// TODO: Auto-generated Javadoc
/**
 * The Class TokenStoreImpl.
 */
@Component
public class TokenStoreImpl implements TokenStore {

  // key token
  private static Map<String, AuthInfo> tokenStorage = new HashMap<>();

  // private static AuthInfo testAuthInfo = new AuthInfo("TEST_ISIKUKOOD", "TEST_ASUTUS", "TEST_ROLL");
  private static AuthInfo testAuthInfo = new AuthInfo("TEST_ISIKUKOOD", "TEST_ASUTUS", "ROLL_RIHA_ADMINISTRAATOR");

  private static boolean isTest = false;

  private static final int NUM_OF_TIMEOUT_MAPS = 3;

  // 3 maps: 1 to be deleted, 1 buffer, 1 where to put new ones
  private static List<Map<String, String>> timeOutStorage = new ArrayList<>(NUM_OF_TIMEOUT_MAPS);

  // public static final long TIME_OUT_IN_MS = 3600 * 1000L;
  public static final long TIME_OUT_IN_MS = Long.valueOf(PropsReader.get("TIME_OUT_IN_MS"));

  private static final int MAX_INDEX = 2;

  private static long timeOutInMS = TIME_OUT_IN_MS;

  private static int saveIndex = 0;

  private static int counter = 0;

  private static Date startNew = new Date();

  private static final Logger LOG = LoggerFactory.getLogger(TokenStoreImpl.class);

  /**
   * Next.
   *
   * @param x the x
   * @return the int
   */
  public static int next(int x) {
    if (x + 1 > MAX_INDEX) {
      return 0;
    } else {
      return x + 1;
    }
  }

  /**
   * Previous.
   *
   * @param x the x
   * @return the int
   */
  public static int previous(int x) {
    if (x - 1 < 0) {
      return MAX_INDEX;
    } else {
      return x - 1;
    }
  }

  static {
    timeOutStorage.add(new HashMap<String, String>());
    timeOutStorage.add(new HashMap<String, String>());
    timeOutStorage.add(new HashMap<String, String>());
  }

  // LOGIC
  // 1. token comes in
  // 2. save token in tokenStorage
  // 3. save token in timeOutStorage.get(saveIndex)
  // 4. if timeout amount of time has passed then saveindex++
  // 5. if timeout passes 2nd time then saveIndex++
  // and empty timeOutStorage(saveIndex.previous.prevuois)
  // 6. after that every timeout one will be emptied
  // 7. if tokenExists called then save timeOut in new, delete in old
  // 8. during emptying corresponding entries must be deleted in tokenStorage
  // 9. clear clears all

  /**
   * List put and remove old.
   *
   * @param token the token
   */
  private static void listPutAndRemoveOld(String token) {
    String existing = timeOutStorage.get(saveIndex).put(token, token);
    String removed = timeOutStorage.get(previous(saveIndex)).remove(token);
    LOG.info("EXISTING: " + existing);
    LOG.info("REMOVED: " + removed);
  }

  /**
   * List get and update.
   *
   * @param token the token
   * @return true, if successful
   */
  private static boolean listGetAndUpdate(String token) {
    boolean exists = false;
    if (timeOutStorage.get(saveIndex).get(token) != null) {
      exists = true;
    } else {
      if (timeOutStorage.get(previous(saveIndex)).get(token) != null) {
        listPutAndRemoveOld(token);
        exists = true;
      }
    }
    return exists;
  }

  /**
   * Delete old.
   *
   * @param deleteIndex the delete index
   */
  private static void deleteOld(int deleteIndex) {
    LOG.info("DELETEING ALL in " + deleteIndex + " : " + timeOutStorage.get(deleteIndex));
    for (String token : timeOutStorage.get(deleteIndex).keySet()) {
      getTokenStorage().remove(token);
    }
    timeOutStorage.get(deleteIndex).clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.auth.TokenStore#addToken(java.lang.String, ee.eesti.riha.rest.auth.AuthInfo)
   */
  @Override
  public void addToken(String token, AuthInfo authInfo) {
    Date current = new Date();
    if (current.getTime() > startNew.getTime() + timeOutInMS) {
      saveIndex = next(saveIndex);
      counter++;
      startNew = current;
      if (counter >= 2) {
        deleteOld(previous(previous(saveIndex)));
      }
    }
    getTokenStorage().put(token, authInfo);
    LOG.info("ADD TOKEN CALLED: " + token);

    listPutAndRemoveOld(token);
    LOG.info("AFTER ADD ");
    printTimeoutStorage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.auth.TokenStore#tokenExists(java.lang.String)
   */
  @Override
  public AuthInfo tokenExists(String token) {
    // to turn off token validation for tests
    if (isTest) {
      LOG.info("TOKEN EXISTS TEST CALLED: " + token);
      return testAuthInfo;
    }
    if (Finals.IS_TEST && StringHelper.areEqual(token, Finals.TEST_TOKEN)) {
      AuthInfo authInfo = getTokenStorage().get(token);
      if (authInfo != null) {
        return authInfo;
      }
      return testAuthInfo;
    }
    LOG.info("TOKEN EXISTS CALLED: " + token);

    // remove if too old
    if (!listGetAndUpdate(token)) {
      getTokenStorage().remove(token);
    }

    return getTokenStorage().get(token);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.auth.TokenStore#clear()
   */
  @Override
  public void clear() {
    LOG.info("CLEAR TOKEN CALLED");
    getTokenStorage().clear();
    for (Map<String, String> map : timeOutStorage) {
      map.clear();
    }
    counter = 0;
    saveIndex = 0;
    startNew = new Date();
    LOG.info("" + getTokenStorage());
    LOG.info("" + getTimeOutStorage());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TokenStoreImpl:: " + getTokenStorage();
  }

  /**
   * Prints the timeout storage.
   */
  public void printTimeoutStorage() {
    LOG.info("TokenStore.tos :: " + timeOutStorage);
  }

  /**
   * Checks if is test.
   *
   * @return true, if is test
   */
  public static boolean isTest() {
    return isTest;
  }

  /**
   * Sets the test.
   *
   * @param aIsTest the new test
   */
  public static void setTest(boolean aIsTest) {
    TokenStoreImpl.isTest = aIsTest;
  }

  /**
   * Gets the time out storage.
   *
   * @return the time out storage
   */
  public static List<Map<String, String>> getTimeOutStorage() {
    return timeOutStorage;
  }

  /**
   * Gets the token storage.
   *
   * @return the token storage
   */
  public static Map<String, AuthInfo> getTokenStorage() {
    return tokenStorage;
  }

  /**
   * Gets the time out in ms.
   *
   * @return the time out in ms
   */
  public static long getTimeOutInMS() {
    return timeOutInMS;
  }

  /**
   * Sets the time out in ms.
   *
   * @param aTimeOutInMS the new time out in ms
   */
  public static void setTimeOutInMS(long aTimeOutInMS) {
    TokenStoreImpl.timeOutInMS = aTimeOutInMS;
  }

}
