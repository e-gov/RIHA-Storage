package ee.eesti.riha.rest.dao.util;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
public class ExcelToGsonConverterTest {
  private FileResource fileResource;
  private ExcelToGsonConverter excelToGsonConverter;

  @BeforeEach
  public void setUp() throws IOException, SQLException {
    excelToGsonConverter = new ExcelToGsonConverter();
    ReflectionTestUtils.setField(excelToGsonConverter, "csvToGsonConverter", new CsvToGsonConverter());

    File excelFile = new File("src/test/resources/xlsx/test1.xlsx");

    Blob blob = Mockito.mock(Blob.class);
    when(blob.getBytes(anyInt(), anyInt())).thenReturn(IOUtils.toByteArray(new FileInputStream(excelFile)));

    LargeObject largeObject = new LargeObject();
    largeObject.setData(blob);

    fileResource = new FileResource();
    fileResource.setLargeObject(largeObject);
    fileResource.setUuid(UUID.randomUUID());
    fileResource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    fileResource.setName("test1.xlsx");
    fileResource.setInfoSystemUuid(UUID.randomUUID());
  }

  @Test
  public void testConvert() throws IOException, SQLException {
    JsonObject result = excelToGsonConverter.convert(fileResource);

    Assertions.assertEquals("[\"Vanemobjekt 3\",\"Vanemobjekt 2\",\"Vanemobjekt 1\",\"Andmeobjekti nimi\",\"IA\",\"DIA\",\"PA\",\"AV\",\"Infosüsteem\",\"Kommentaar\"]",
        result.getAsJsonArray("headers").toString());
  }
}
