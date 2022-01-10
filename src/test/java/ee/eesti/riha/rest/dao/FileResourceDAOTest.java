package ee.eesti.riha.rest.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/test-applicationContext.xml")
public class FileResourceDAOTest extends AbstractGenericDaoTest {
    @Autowired
    private LargeObjectDAO largeObjectDAO;

    @Autowired
    private FileResourceDAO fileResourceDAO;

    @Test
    public void shouldGetByUuidAndInfoSystemUuid() throws Exception {
        UUID infoSystemUuid = UUID.randomUUID();
        UUID uuid = createFileResource(infoSystemUuid);

        FileResource result = fileResourceDAO.get(uuid, infoSystemUuid);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
    }

    @Test
    public void shouldFindUnindexed() throws Exception {
        UUID infoSystemUuid = UUID.randomUUID();
        UUID uuid = createFileResource(infoSystemUuid);

        List<FileResource> result = fileResourceDAO.getUnindexedFiles();

        assertEquals(1, result.size());
        assertEquals(uuid, result.get(0).getUuid());
    }

    private UUID createFileResource(UUID infoSystemUuid) throws Exception {
        File excelFile = new File("src/test/resources/xlsx/test1.xlsx");

        int largeObjectId = largeObjectDAO.create(new FileInputStream(excelFile));
        LargeObject largeObject = largeObjectDAO.get(largeObjectId);

        FileResource fileResource = new FileResource();
        fileResource.setLargeObject(largeObject);
        fileResource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileResource.setName("test1.xlsx");
        fileResource.setInfoSystemUuid(infoSystemUuid);

        return fileResourceDAO.create(fileResource);
    }
}
