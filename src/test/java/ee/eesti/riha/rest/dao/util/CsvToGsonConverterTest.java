package ee.eesti.riha.rest.dao.util;

import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CsvToGsonConverterTest {

    private CsvToGsonConverter csvToGsonConverter = new CsvToGsonConverter();

    private FileResource fileResource;
    private byte[] content = "header1,header2\r\nvalue-1-1,value-2-1".getBytes();

    @Before
    public void setUp() throws SQLException {
        Blob blob = Mockito.mock(Blob.class);
        Mockito.when(blob.getBinaryStream()).thenReturn(new ByteArrayInputStream(content));

        LargeObject largeObject = new LargeObject();
        largeObject.setData(blob);

        fileResource = new FileResource();
        fileResource.setContentType("text/csv");
        fileResource.setLargeObject(largeObject);
    }

    @Test
    public void supportsCsvMimeType() {
        assertThat(csvToGsonConverter.supports(fileResource), is(true));
    }

    @Test
    public void supportsResourcesWithCsvFileExtension() {
        fileResource.setContentType("application/pdf");
        fileResource.setName("important-doc.csv");

        assertThat(csvToGsonConverter.supports(fileResource), is(true));
    }

    @Test
    public void doesNotSupportUnknownMimeType() {
        fileResource.setContentType("application/pdf");

        assertThat(csvToGsonConverter.supports(fileResource), is(false));
    }

}
