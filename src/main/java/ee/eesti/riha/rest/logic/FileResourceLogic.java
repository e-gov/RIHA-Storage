package ee.eesti.riha.rest.logic;

import ee.eesti.riha.rest.dao.FileResourceDAO;
import ee.eesti.riha.rest.dao.LargeObjectDAO;
import ee.eesti.riha.rest.model.FileResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Component
public class FileResourceLogic {

    @Autowired
    private LargeObjectDAO largeObjectDAO;

    @Autowired
    private FileResourceDAO fileResourceDAO;

    /**
     * Creates {@link FileResource} from provided {@link InputStream} with file name and content.
     *
     * @param inputStream data input stream
     * @param name        resource name
     * @param contentType resource content type
     * @return UUID of created file resource
     */
    public UUID create(InputStream inputStream, String name, String contentType) {
        int lobId = largeObjectDAO.create(inputStream);

        FileResource entity = new FileResource();
        entity.setName(name);
        entity.setContentType(contentType);

        entity.setLargeObject(largeObjectDAO.load(lobId));

        return fileResourceDAO.create(entity);
    }
}
