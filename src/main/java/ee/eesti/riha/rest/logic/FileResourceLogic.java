package ee.eesti.riha.rest.logic;

import ee.eesti.riha.rest.dao.FileResourceDAO;
import ee.eesti.riha.rest.dao.LargeObjectDAO;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
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
    @Transactional
    public UUID create(InputStream inputStream, String name, String contentType) {
        int largeObjectId = largeObjectDAO.create(inputStream);
        LargeObject largeObject = largeObjectDAO.get(largeObjectId);
        if (largeObject == null) {
            throw new IllegalStateException("LargeObject with id " + largeObjectId + " is not found");
        }

        FileResource entity = new FileResource();
        entity.setName(name);
        entity.setContentType(contentType);
        entity.setLargeObject(largeObject);

        return fileResourceDAO.create(entity);
    }

    @Transactional
    public FileResource get(UUID uuid) {
        return fileResourceDAO.get(uuid);
    }

    @Transactional
    public void copyLargeObjectData(UUID uuid, OutputStream output) throws SQLException, IOException {
        FileResource fileResource = get(uuid);
        if (fileResource == null) {
            throw new IllegalStateException("FileResource with id " + uuid.toString() + " is not found");
        }

        StreamUtils.copy(fileResource.getLargeObject().getData().getBinaryStream(), output);
    }
}
