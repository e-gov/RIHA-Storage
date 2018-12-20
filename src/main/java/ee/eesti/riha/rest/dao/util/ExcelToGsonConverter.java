package ee.eesti.riha.rest.dao.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts Excel content from {@link LargeObject} of {@link FileResource} to {@link JsonNode}.
 */
@Component
public class ExcelToGsonConverter implements ToGsonConverter {
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
    private static final List<MediaType> SUPPORTED_MEDIA_TYPES = Collections.singletonList(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    private static final String EXCEL_FILE_SUFFIX = ".xls";
    private static final String EXCELX_FILE_SUFFIX = ".xlsx";

    @Autowired
    private CsvToGsonConverter csvToGsonConverter;

    @Override
    public boolean supports(FileResource fileResource) {
        return SUPPORTED_MEDIA_TYPES.contains(MediaType.valueOf(fileResource.getContentType()))
                || StringUtils.endsWithIgnoreCase(fileResource.getName(), EXCEL_FILE_SUFFIX)
                || StringUtils.endsWithIgnoreCase(fileResource.getName(), EXCELX_FILE_SUFFIX);
    }

    @Override
    public JsonObject convert(FileResource fileResource) throws IOException, SQLException {
        Blob blob = fileResource.getLargeObject().getData();
        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(blob.getBytes(1, ((int) blob.length()))));
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

        DataFormatter formatter = new DataFormatter();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream, true, "UTF-8");

        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        printStream.write(bom);

        for (int sheetNumber = 0; sheetNumber < workbook.getNumberOfSheets(); sheetNumber++) {
            Sheet sheet = workbook.getSheetAt(sheetNumber);

            for (int rowNumber = 0; rowNumber <= sheet.getLastRowNum(); rowNumber++) {
                Row row = sheet.getRow(rowNumber);
                if (row == null) {
                    printStream.println(CsvToGsonConverter.DELIMITER);
                    continue;
                }

                boolean firstCell = true;
                for (int cellNUmber = 0; cellNUmber < row.getLastCellNum(); cellNUmber++) {
                    Cell cell = row.getCell(cellNUmber, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (!firstCell) printStream.print(CsvToGsonConverter.DELIMITER);

                    if (cell != null) {
                        cell = formulaEvaluator.evaluateInCell(cell);
                        String value = formatter.formatCellValue(cell);
                        if (cell.getCellType() == CellType.FORMULA) {
                            value = "=" + value;
                        }
                        printStream.print(encodeValue(value));
                    }
                    firstCell = false;
                }
                printStream.println();
            }
        }

        fileResource.getLargeObject().setData(new SerialBlob(byteArrayOutputStream.toByteArray()));

        return csvToGsonConverter.convert(fileResource);
    }

    static private String encodeValue(String value) {
        boolean needQuotes = false;

        if (value.indexOf(CsvToGsonConverter.DELIMITER) != -1 || value.indexOf('"') != -1 ||
                value.indexOf('\n') != -1 || value.indexOf('\r') != -1) {
            needQuotes = true;
        }

        Matcher m = QUOTE_PATTERN.matcher(value);
        if (m.find()) {
            needQuotes = true;
        }
        value = m.replaceAll("\"\"");

        return needQuotes ? "\"" + value + "\"" : value;
    }
}