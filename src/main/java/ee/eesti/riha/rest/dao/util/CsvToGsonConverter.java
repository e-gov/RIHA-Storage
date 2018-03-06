package ee.eesti.riha.rest.dao.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.util.DateHelper;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Converts CSV content from {@link LargeObject} of {@link FileResource} to {@link JsonNode}.
 */
@Component
public class CsvToGsonConverter {

    private static final Logger logger = LoggerFactory.getLogger(CsvToGsonConverter.class);

    private static final String CSV_FILE_SUFFIX = ".csv";
    private static final CSVFormat DEFAULT_WITH_HEADERS = CSVFormat.DEFAULT
            .withDelimiter(';')
            .withFirstRecordAsHeader()
            .withIgnoreEmptyLines()
            .withIgnoreSurroundingSpaces();

    private static List<MediaType> supportedMediaTypes = Arrays.asList(MediaType.valueOf("text/csv"));

    public boolean supports(FileResource fileResource) {
        return supportedMediaTypes.contains(MediaType.valueOf(fileResource.getContentType()))
                || StringUtils.endsWithIgnoreCase(fileResource.getName(), CSV_FILE_SUFFIX);
    }

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
     */
    public JsonObject convert(FileResource fileResource) throws IOException, SQLException {
        logger.debug("Starting file resource '{}' CSV to JSON conversion", fileResource.getUuid());

        CSVParser parser = getFormat(fileResource)
                .parse(new InputStreamReader(
                        new BOMInputStream(fileResource.getLargeObject().getData().getBinaryStream()), "UTF-8"));

        JsonObject rootNode = new JsonObject();
        rootNode.add("metadata", getMetadata(fileResource));
        rootNode.add("headers", convertHeaders(parser));
        rootNode.add("records", convertRecords(parser));

        return rootNode;
    }

    /**
     * Return {@link CSVFormat} according to {@link FileResource} parameters.
     *
     * @param fileResource file resource
     * @return corresponding CSV format for parsing
     */
    protected CSVFormat getFormat(FileResource fileResource) {
        return DEFAULT_WITH_HEADERS;
    }

    /**
     * Constructs json representation of {@link FileResource} metadata.
     *
     * @param fileResource file resource
     * @return instance of json object with file resource meta data
     */
    protected JsonObject getMetadata(FileResource fileResource) {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("uuid", fileResource.getUuid().toString());
        metadata.addProperty("file_name", fileResource.getName());
        metadata.addProperty("content_type", fileResource.getContentType());
        metadata.addProperty("info_system_uuid", fileResource.getInfoSystemUuid().toString());
        if (fileResource.getCreationDate() != null) {
            metadata.addProperty("creation_date", DateHelper.FORMATTER.format(fileResource.getCreationDate()));
        }

        return metadata;
    }

    /**
     * Converts headers from CSV headers to {@link JsonArray}. Each header is represented as string value. Order is not
     * guaranteed.
     *
     * @param parser CSV parser
     * @return header {@link JsonArray}
     */
    protected JsonArray convertHeaders(CSVParser parser) {
        JsonArray headerNode = new JsonArray();
        Map<String, Integer> headerMap = parser.getHeaderMap();
        for (String header : headerMap.keySet()) {
            headerNode.add(header);
        }

        return headerNode;
    }

    /**
     * Converts parsed CSV records to {@link JsonArray} of {@link JsonObject}s. Each CSV record is represented as {@link
     * JsonObject} with properties where key is record value corresponding header. Ordering is not guaranteed.
     *
     * @param parser CSV parser
     * @return instance of {@link JsonArray} with every record as object
     */
    protected JsonArray convertRecords(CSVParser parser) {
        JsonArray recordArray = new JsonArray();
        for (CSVRecord record : parser) {
            JsonObject recordObjectNode = new JsonObject();
            for (Map.Entry<String, String> recordEntry : record.toMap().entrySet()) {
                recordObjectNode.addProperty(recordEntry.getKey(), recordEntry.getValue());
            }
            recordArray.add(recordObjectNode);
        }

        return recordArray;
    }

}
