package ee.eesti.riha.rest.model.hibernate;

import org.hibernate.dialect.PostgreSQL9Dialect;

import java.sql.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class JsonPostgreSQLDialect.
 */
public class JsonPostgreSQLDialect extends PostgreSQL9Dialect {

  /**
   * Instantiates a new json postgre sql dialect.
   */
  // http://stackoverflow.com/questions/15974474/mapping-postgresql-json-column-to-hibernate-value-type
  public JsonPostgreSQLDialect() {

    super();

    this.registerColumnType(Types.JAVA_OBJECT, "json");
  }
}
