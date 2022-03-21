package ee.eesti.riha.rest.logic;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import ee.eesti.riha.rest.dao.FileResourceDAO;
import ee.eesti.riha.rest.dao.util.CsvToGsonConverter;
import ee.eesti.riha.rest.dao.util.ExcelToGsonConverter;
import ee.eesti.riha.rest.dao.util.ToGsonConverter;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FileResourceIndexingServiceTest {
    @Mock
    private FileResourceDAO fileResourceDAO;

    @Mock
    private Blob blob;

    @InjectMocks
    private FileResourceIndexingService service;

    private FileResource fileResource;
    private UUID uuid;

    @Before
    public void setup() throws SQLException, IOException {
        CsvToGsonConverter csvToGsonConverter = new CsvToGsonConverter();
        ExcelToGsonConverter excelToGsonConverter = new ExcelToGsonConverter();
        ReflectionTestUtils.setField(excelToGsonConverter, "csvToGsonConverter", csvToGsonConverter);

        List<ToGsonConverter> converters = Arrays.asList(csvToGsonConverter, excelToGsonConverter);
        ReflectionTestUtils.setField(service, "toGsonConverters", converters);

        File excelFile = new File("src/test/resources/xlsx/test1.xlsx");

        when(blob.getBytes(anyLong(), anyInt())).thenReturn(IOUtils.toByteArray(new FileInputStream(excelFile)));

        LargeObject largeObject = new LargeObject();
        largeObject.setData(blob);

        uuid = UUID.randomUUID();
        fileResource = new FileResource();
        fileResource.setLargeObject(largeObject);
        fileResource.setUuid(uuid);
        fileResource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileResource.setName("test1.xlsx");
        fileResource.setInfoSystemUuid(UUID.randomUUID());

        when(fileResourceDAO.get(uuid)).thenReturn(fileResource);
    }

    @Test
    public void indexAsynchronously() throws IOException, SQLException {
        service.indexAsynchronously(uuid);

        Assert.assertEquals("[\"Vanemobjekt 3\",\"Vanemobjekt 2\",\"Vanemobjekt 1\",\"Andmeobjekti nimi\",\"IA\",\"DIA\",\"PA\",\"AV\",\"Infosüsteem\",\"Kommentaar\"]",
                fileResource.getLargeObject().getSearchContent().getAsJsonArray("headers").toString());
    }
}
