package ee.eesti.riha.rest.dao;

/**
 * Any other general/helping query to database collected here.
 *
 * @param <T> the generic type
 */
public interface UtilitiesDAO<T> {

  /**
   * Gets the next seq val for pk for table.
   *
   * @param classRepresentingTable the class representing table
   * @return the next seq val for pk for table
   */
  Integer getNextSeqValForPKForTable(Class<T> classRepresentingTable);

}
