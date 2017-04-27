package ee.eesti.riha.rest.dao;

import java.util.List;
import java.util.Map;

/**
 * Interface to get names from database.
 */
public interface NamesDAO {

  /**
   * Finds organization names for codes [12312, 345345] -> {12312 : Asutus 1, 345345 : Asutus XYZ}.
   *
   * @param organizations the organizations
   * @return the organization names
   */
  Map<String, String> getOrganizationNames(List<String> organizations);

  /**
   * Finds person names for codes [35101011234, 60101011234] -> {35101011234 : Jaan Tamm, 60101011234 : Mari Mets}.
   *
   * @param persons the persons
   * @return the person names
   */
  Map<String, String> getPersonNames(List<String> persons);

  /**
   * Finds names for URIs in Main_resource [urn:fdc:riha.eesti.ee:2016:classifier:172297] ->
   * {urn:fdc:riha.eesti.ee:2016:classifier:172299 : Arstide erialad}
   *
   * @param uris the uris
   * @return the uri names
   */
  Map<String, String> getUriNames(List<String> uris);

  /**
   * Finds names for IDs in Main_resource [172297] ->
   * {172297 : Arstide erialad}
   *
   * @param ids the IDs
   * @return the ID names
   */
  Map<String, String> getIdNames(List<String> ids);
}
