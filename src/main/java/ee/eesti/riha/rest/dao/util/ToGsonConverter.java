package ee.eesti.riha.rest.dao.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Converts file content from {@link LargeObject} of {@link FileResource} to {@link JsonNode}.
 */
public interface ToGsonConverter {
    /**
     * Returns true if content type or file extension of given {@link FileResource} is supported by this converter
     */
    boolean supports(FileResource fileResource);

    /**
     * Converts {@link FileResource} input stream to {@link JsonNode} form
     * <pre>
     * {
     *     "meta": {&lt;used FileResource metadata&gt;}
     *     "headers": [&lt;CSV headers&gt;],
     *     "records": [
     *          {
     *             "header-name": "value",
     *              ...
     *          }
     *     ]
     * }
     * </pre>
     *
     * @param fileResource converted file resource
     * @return created JsonNode
     * @throws IOException in case of parsing errors
     * @throws SQLException
     */
    JsonObject convert(FileResource fileResource) throws IOException, SQLException;
}
