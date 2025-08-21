package ee.eesti.riha.rest.model.hibernate;

import org.hibernate.dialect.PostgreSQLDialect;

// TODO: Auto-generated Javadoc
/**
 * The Class JsonPostgreSQLDialect.
 */
public class JsonPostgreSQLDialect extends PostgreSQLDialect {

  /**
   * Instantiates a new json postgre sql dialect.
   */
  // http://stackoverflow.com/questions/15974474/mapping-postgresql-json-column-to-hibernate-value-type
  public JsonPostgreSQLDialect() {
    super();
    // In Hibernate 6, JSON types are supported natively for PostgreSQL
    // No need for custom registration
  }
}
