package ee.eesti.riha.rest.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.model.readonly.Asutus;
import ee.eesti.riha.rest.model.readonly.Isik;
import ee.eesti.riha.rest.util.PropsReader;

// TODO: Auto-generated Javadoc
/**
 * The Class Finals.
 */
public final class Finals {

  private Finals() {

  }

  static final Map<String, Class<?>> TABLE_CLASS_MAP = new HashMap<>();
  static final Map<String, Class<?>> TABLE_CLASS_MAP_READONLY = new HashMap<>();

  static {
    // add existing tables here, that you expect
    // rest to work with
    addClassRepresentingTable(Main_resource.class);
    addClassRepresentingTable(Document.class);
    addClassRepresentingTable(Data_object.class);
    addClassRepresentingTable(Comment.class);

    // read only
    addClassRepresentingTable(Isik.class);
    addClassRepresentingTable(Asutus.class);
    TABLE_CLASS_MAP_READONLY.put(Isik.class.getSimpleName().toLowerCase(), Isik.class);
    TABLE_CLASS_MAP_READONLY.put(Asutus.class.getSimpleName().toLowerCase(), Asutus.class);
  }

  /**
   * Gets the class representing table.
   *
   * @param <C> the generic type
   * @param tableName the table name
   * @return the class representing table
   */
  public static <C> Class<C> getClassRepresentingTable(String tableName) {
    return (Class<C>) TABLE_CLASS_MAP.get(tableName.toLowerCase());
  }

  /**
   * Gets the class representing table read only.
   *
   * @param <C> the generic type
   * @param tableName the table name
   * @return the class representing table read only
   */
  public static <C> Class<C> getClassRepresentingTableReadOnly(String tableName) {
    return (Class<C>) TABLE_CLASS_MAP_READONLY.get(tableName.toLowerCase());
  }

  /**
   * Adds the class representing table.
   *
   * @param <C> the generic type
   * @param clazz the clazz
   */
  private static <C> void addClassRepresentingTable(Class<C> clazz) {
    TABLE_CLASS_MAP.put(clazz.getSimpleName().toLowerCase(), clazz);
  }

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  public static final String OK = "ok";
  public static final String KEY = "key";
  // default limit for returned items
  // public static final int NUM_OF_ITEMS_IN_RESULT_ALLOWED = 100;
  public static final int NUM_OF_ITEMS_IN_RESULT_ALLOWED = Integer.MAX_VALUE;

  // if limit == -1 then ignore NUM_OF_ITEMS_IN_RESULT_ALLOWED in count
  public static final int COUNT_ALL_LIMIT = -1;

  // known parameters
  public static final String OP = "op";
  public static final String PATH = "path";
  public static final String DATA = "data";
  public static final String TOKEN = "token";
  public static final String FILTER = "filter";
  public static final String CALLBACK = "callback";
  public static final String[] KNOWN_PARAMETERS = {OP, PATH, TOKEN, CALLBACK };

  public static final String PATH_ROOT = "db";

  // query operations
  public static final String GET = "get";
  public static final String POST = "post";
  public static final String PUT = "put";
  public static final String DELETE = "delete";
  public static final String COUNT = "count";
  public static final String GET_NAMES = "getnames";
  public static final String NEW_VERSION = "newversion";

  public static final String FILTER_ITEMS_SEPARATOR = ",";
  public static final int NUM_OF_FILTER_ITEMS = 3;

  // Infosystem fields
  public static final String NAME = "name";
  public static final String INFOSYSTEM = "infosystem";

  // main_resource fields
  public static final String MAIN_RESOURCE_CREATOR = "creator";
  public static final String MAIN_RESOURCE_ORGANIZATION = "organization";
  public static final String MAIN_RESOURCE_JSON_CONTENT = "json_content";

  // field reoccuring in different models
  public static final String JSON_CONTENT = "json_content";
  public static final String KIND = "kind";
  public static final String MODIFIER = "modifier";
  public static final String MODIFIED_DATE = "modified_date";

  public static final String ID = "id";
  public static final String KIND_ID = "kind_id";
  public static final String MAIN_RESOURCE_ID = "main_resource_id";

  public static final String DEFAULT_DOC = "default_documents";
  public static final String DEFAULT_DATA = "default_data_objects";
  public static final String DEFAULT_MAIN_RESOURCE = "default_main_resources";

  // header fields
  public static final String X_AUTH_TOKEN = "X-Auth-Token";

  // for testing purposes
  static final String DEFAULT_MODIFIER = "TEST";

  public static final boolean IS_TEST = StringHelper.areEqual(PropsReader.get("IS_TEST"), "true");
  public static final String TEST_TOKEN = "testToken";

  /**
   * Gets the table class map.
   *
   * @return the table class map
   */
  public static Map<String, Class<?>> getTableClassMap() {
    return TABLE_CLASS_MAP;
  }

  // only op="get" allowed here ! as no changes in db allowed (all
  // other operations (post, put, delete) change database)
  static final List<String> GET_CGI_ALLOWED_VALUES = new ArrayList<>();
  static final List<String> POST_CGI_ALLOWED_VALUES = new ArrayList<>();
  static final List<String> READ_ALLOWED_VALUES = new ArrayList<>();

  static {
    GET_CGI_ALLOWED_VALUES.add(GET.toLowerCase());
    GET_CGI_ALLOWED_VALUES.add(COUNT.toLowerCase());

    POST_CGI_ALLOWED_VALUES.add(GET.toLowerCase());
    POST_CGI_ALLOWED_VALUES.add(POST.toLowerCase());
    POST_CGI_ALLOWED_VALUES.add(PUT.toLowerCase());
    POST_CGI_ALLOWED_VALUES.add(DELETE.toLowerCase());
    POST_CGI_ALLOWED_VALUES.add(COUNT.toLowerCase());
    POST_CGI_ALLOWED_VALUES.add(GET_NAMES.toLowerCase());
    POST_CGI_ALLOWED_VALUES.add(NEW_VERSION.toLowerCase());

    READ_ALLOWED_VALUES.add(GET.toLowerCase());
    READ_ALLOWED_VALUES.add(COUNT.toLowerCase());
    READ_ALLOWED_VALUES.add(GET_NAMES.toLowerCase());
  }
}
