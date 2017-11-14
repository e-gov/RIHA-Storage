package ee.eesti.riha.rest.error;

/**
 * Class for all error codes and messages
 */
public final class ErrorCodes {

  /**
   * Instantiates a new error codes.
   */
  private ErrorCodes() {

  }

  // techincal errors
  public static final int CLIENT_TIMEOUT = 3;
  public static final String CLIENT_TIMEOUT_MSG = "Päringu lugemine võttis liiga kaua aega - timeout";
  public static final int DB_CONNECTION_ERROR = 4;
  public static final String DB_CONNECTION_ERROR_MSG = "Andmebaasiga ühenduse loomine ebaõnnestus";

  // error codes general
  public static final int INPUT_CAN_NOT_FIND_COLUMN = 13;
  public static final String INPUT_CAN_NOT_FIND_COLUMN_MSG = "Sisendi viga. Tabel ei sisalda välja: ";
  public static final int INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED = 14;
  public static final String INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG = "Sisendi viga. Päring tundmatule tabelile";
  public static final int INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID = 15;
  public static final String INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID_MSG = "Sisendi viga. Soovitud id'ga kirjet ei leitud";

  // URL parsing related error codes
  public static final int INPUT_FILTER_MUST_HAVE_3_ITEMS_PER_GROUP = 16;
  public static final String INPUT_FILTER_MUST_HAVE_3_ITEMS_PER_GROUP_MSG = "Sisendi viga. "
      + "Parameeter filter - igas filtri grupis peab olema 3 elementi";
  public static final int INPUT_URL_OP_VALUE_UNKNOWN_OR_NOTSUITABLE = 17;
  public static final String INPUT_URL_OP_VALUE_UNKNOWN_OR_NOTSUITABLE_MSG = "Sisendi viga. "
      + "URL op väärtus tundmatu/sobimatu";
  public static final int INPUT_URL_PATH_VALUE_NOTVALID = 18;
  public static final String INPUT_URL_PATH_VALUE_NOTVALID_MSG = "Sisendi viga. URL path väärtus sobimatu";
  public static final int INPUT_URL_REQUIRED_ATTRIBUTES_MISSING = 19;
  public static final String INPUT_URL_REQUIRED_ATTRIBUTES_MISSING_MSG = "Sisendi viga. "
      + "Puudu vajalikud URL parameetrid: ";

  // JSON parsing related error codes
  public static final int INPUT_JSON_MISSING = 20;
  public static final String INPUT_JSON_MISSING_MSG = "Sisendi viga. JSON puudu";
  public static final int INPUT_JSON_NOT_VALID_JSON = 21;
  public static final String INPUT_JSON_NOT_VALID_JSON_MSG = "Sisendi viga. JSON'i süntaks vigane";
  public static final int INPUT_JSON_REQUIRED_PROPERTIES_MISSING = 22;
  public static final String INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG = "Sisendi viga. "
      + "JSON ei sisaldanud järgmisi vajalikke parameetreid: ";
  public static final int INPUT_JSON_LIST_ERRORS = 23;
  public static final String INPUT_JSON_LIST_ERRORS_MSG = "Sisendi viga. Viga JSON listis";
  public static final int INPUT_JSON_PATH_VALUE_NOTVALID = 24;
  public static final String INPUT_JSON_PATH_VALUE_NOTVALID_MSG = "Sisendi viga. JSON path väärtus sobimatu";
  public static final int INPUT_JSON_OP_VALUE_UNKNOWN = 25;
  public static final String INPUT_JSON_OP_VALUE_UNKNOWN_MSG = "Sisendi viga. JSON op väärtus tundmatu. "
      + "PS! Callback ja eripäringud ei tööta!";
  public static final int INPUT_JSON_ARRAY_RECEIVED_BUT_CAN_ACCEPT_SINGLE_JSON_OBJ_ONLY = 26;
  public static final String INPUT_JSON_ARRAY_RECEIVED_BUT_CAN_ACCEPT_SINGLE_JSON_OBJ_ONLY_MSG = "Sisendi viga. "
      + "See meetod saab vastu võtta vaid 1 element korraga (vt JSON)";
  public static final int INPUT_JSON_GENERAL_SOMETHING_MISSING = 27;
  public static final String INPUT_JSON_GENERAL_SOMETHING_MISSING_MSG = "Sisendi viga. JSON ei sisalda: ";
  public static final int JSON_TYPE_ERROR = 28;
  public static final String JSON_TYPE_ERROR_MSG = "Sisendi viga. JSONi tüüp on vigane";
  public static final int UPDATE_ID_MISSING = 29;
  public static final String UPDATE_ID_MISSING_MSG = "Sisendi viga. Ühe elemendi uuendamiseks peab path-is olema id,"
      + " mitme elemendi uuendamiseks peab data olema array";

  public static final int FILTER_OP_VALUE_MUST_BE_ARRAY = 33;
  public static final String FILTER_OP_VALUE_MUST_BE_ARRAY_MSG = "Sisendi viga. "
      + "Selle op-iga peab filtri väärtus olema array";

  // authentication problems
  public static final int NO_HTTP_AUTH_TOKEN_PROVIDED = 50;
  public static final String NO_HTTP_AUTH_TOKEN_PROVIDED_MSG = "HTTP päise viga. Puudub X-Auth_token.";
  public static final int NO_AUTH_TOKEN_PROVIDED = 51;
  public static final String NO_AUTH_TOKEN_PROVIDED_MSG = "Päringu viga. Puudub token parameeter/väli (X-Auth_token).";
  public static final int AUTH_TOKEN_INVALID = 52;
  public static final String AUTH_TOKEN_INVALID_MSG = "X-Auth-token ei ole korrektne/kehtiv";
  public static final int CANT_CONNECT_TO_AUTH = 53;
  public static final String CANT_CONNECT_TO_AUTH_MSG = "Ei saa autentimise tokeni valideerimise teenusega ühendust";
  public static final int THIRD_PARTY_AUTH_TOKEN_INVALID = 54;
  public static final String THIRD_PARTY_AUTH_TOKEN_INVALID_MSG = "Riha autentimsteenus ei leidnud antud "
      + "sessiooni id-ga seotud kasutajat";

  public static final int DOCUMENT_FILE_READ_ERROR = 55;
  public static final String DOCUMENT_FILE_READ_ERROR_MSG = "Tabeli Document faili sisu lugemine ebaõnnestus";
  public static final int DOCUMENT_CREATE_HAS_NO_REF = 56;
  public static final String DOCUMENT_CREATE_HAS_NO_REF_MSG = "Document-il peab  main_resource_id või "
      + "data_object_id olema väärtustatud";
  public static final int DOCUMENT_FILE_NOT_FOUND = 57;
  public static final String DOCUMENT_FILE_NOT_FOUND_MSG = "Dokumendi faili ei leitud";
  
  public static final int INPUT_EXPECTED_INTEGER = 60;
  public static final String INPUT_EXPECTED_INTEGER_MSG = "Sisendi viga. Parameeter peab olema Integer, "
      + "aga on String: ";
  public static final int URL_ENCODING_PERCENT = 61;
  public static final String URL_ENCODING_PERCENT_MSG = "Sisendi viga. % asemel tuleb URLis kasutada %25";
  public static final int DATE_FORMAT_ERROR = 62;
  public static final String DATE_FORMAT_ERROR_MSG = "Kuupäeva formaat pole korrektne ";
  public static final int KIND_NOT_FOUND = 65;
  public static final String KIND_NOT_FOUND_MSG = "Sellist kind'i ei eksisteeri";
  // sql errors
  public static final int VARCHAR_TOO_LONG_ERROR = 68;
  public static final String VARCHAR_TOO_LONG_ERROR_MSG = "Andmevälja väärtus on liiga pikk, maksimum: ";
  public static final int SQL_TRIGGER_ERROR = 69;
  public static final String SQL_TRIGGER_ERROR_MSG = "SQL trigeri viga: ";
  public static final int SQL_ERROR = 70;
  public static final String SQL_ERROR_MSG = "SQL päring on vigane ";
  public static final int FOREIGN_KEY_VIOLATION = 71;
  public static final String FOREIGN_KEY_VIOLATION_MSG = "Välisvõtme kitsenduse viga";
  public static final int CONSTRAINT_VIOLATION = 72;
//  public static final String CONSTRAINT_VIOLATION_MSG = "Andmete kitsendust on rikutud";
  public static final String CONSTRAINT_VIOLATION_MSG = "Andmeväli ei vasta nõutud tingimustele";
  public static final int SQL_NO_SUCH_OPERATOR_EXISTS = 73;
  public static final String SQL_NO_SUCH_OPERATOR_EXISTS_MSG = "Op peab olema "
      + "(=, >, <, >=, <=, !=, <>, like, ilike, ?&, null_or_>, isnull, isnotnull, jilike, jarr), aga on: ";
  public static final int CAN_UPDATE_VERSION_HERE = 74;
  public static final String CAN_UPDATE_VERSION_HERE_MSG = "Versiooni ei ole võimalik selle meetodiga muuta";

  public static final int NO_ITEM_WITH_URI_FOUND = 75;
  public static final String NO_ITEM_WITH_URI_FOUND_MSG = "Sisendi viga. Soovitud uri-ga kirjet ei leitud";

  public static final int VERSION_MUST_BE_UPDATED = 76;
  public static final String VERSION_MUST_BE_UPDATED_MSG = "Uus version peab olema erinev, antud versiooniga "
      + "uri on juba kasutuses: ";
  public static final int CANT_UPDATE_ARCHIVED = 77;
  public static final String CANT_UPDATE_ARCHIVED_MSG = "Mittekehtivat versiooni ei saa muuta";
  public static final int CANT_CREATE_NEW_VERSION = 78;
  public static final String CANT_CREATE_NEW_VERSION_MSG = "Ainult Main_resource'st saab luua uusi versioone";

  public static final int TABLE_CANT_BE_MODIFIED = 80;
  public static final String TABLE_CANT_BE_MODIFIED_MSG = "Antud tabelit ei saa muuta, saab ainult lugeda";
  public static final int WRONG_TABLE_FULL_SERVICE = 81;
  public static final String WRONG_TABLE_FULL_SERVICE_MSG = "Pole sobiv tabel antud teenuse jaoks";

  public static final int NOT_AUTHORIZED_FOR_CREATE = 90;
  public static final String NOT_AUTHORIZED_FOR_CREATE_MSG = "Ligipääs keelatud! Puudub lisamisõigus ";
  public static final int NOT_AUTHORIZED_FOR_READ = 91;
  public static final String NOT_AUTHORIZED_FOR_READ_MSG = "Ligipääs keelatud! Puudub lugemisõigus ";
  public static final int NOT_AUTHORIZED_FOR_UPDATE = 92;
  public static final String NOT_AUTHORIZED_FOR_UPDATE_MSG = "Ligipääs keelatud! Puudub muutmisõigus ";
  public static final int NOT_AUTHORIZED_FOR_DELETE = 93;
  public static final String NOT_AUTHORIZED_FOR_DELETE_MSG = "Ligipääs keelatud! Puudub kustutamisõigus ";
  public static final int NOT_AUTHORIZED_NO_REF_MAIN_RESOURECE = 94;
  public static final String NOT_AUTHORIZED_NO_REF_MAIN_RESOURECE_MSG = "Ligipääs keelatud! "
      + "Ei leitud objektiga seotud Main_resource'i ";

  // special errors
  public static final int UNMAPPED_PROBLEM = 30;
  public static final String UNMAPPED_PROBLEM_MSG = "Ootamatu viga";

  // indicates TODO
  public static final int THIS_PART_NOT_IMPLEMENTED_YET = 31;
  public static final String THIS_PART_NOT_IMPLEMENTED_YET_MSG = "Kahjuks antud osa pole veel realiseeritud";

  // HTTP codes
  public static final int NOT_FOUND = 404;
  public static final String NOT_FOUND_MSG = "Vastust ei leitud";

  public static final int SERVER_ERROR = 500;
  public static final String SERVER_ERROR_MSG = "Serveri poole viga!";

}
