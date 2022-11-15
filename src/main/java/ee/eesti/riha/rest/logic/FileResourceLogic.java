package ee.eesti.riha.rest.logic;

import ee.eesti.riha.rest.dao.FileResourceDAO;
import ee.eesti.riha.rest.dao.LargeObjectDAO;
import ee.eesti.riha.rest.dao.grid.RegisteredFileGrid;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.model.LargeObject;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FileResourceLogic.class);

    @Autowired
    private LargeObjectDAO largeObjectDAO;

    @Autowired
    private FileResourceDAO fileResourceDAO;

    @Autowired
    private RegisteredFileGrid registeredFileGrid;

    @Autowired
    private FileResourceIndexingService fileResourceIndexingService;

    /**
     * Creates {@link FileResource} from provided {@link InputStream} with file name and content.
     *
     * @param inputStream data input stream
     * @param name        resource name
     * @param contentType resource content type
     * @return UUID of created file resource
     */

    @Transactional
    public UUID createFileResource(InputStream inputStream, UUID infoSystemUuid, String name, String contentType) {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating file resource for name: '{}' and content type: '{}'", name, contentType);
        }
        int largeObjectId = largeObjectDAO.create(inputStream);
        logger.info("Created large object id: {}", largeObjectId);

        LargeObject largeObject = largeObjectDAO.get(largeObjectId);
        if (largeObject == null) {
            throw new IllegalStateException("LargeObject with id " + largeObjectId + " is not found");
        }

        FileResource entity = new FileResource();
        entity.setInfoSystemUuid(infoSystemUuid);
        entity.setName(name);
        entity.setContentType(contentType);
        entity.setLargeObject(largeObject);

        UUID uuid = fileResourceDAO.create(entity);
        logger.info("Created file resource '{}'", uuid);

        return uuid;
    }

    /**
     * Creates a copy of {@link FileResource} with new info system UUID
     *
     * @param existingFileUuid UUID of existing file resource
     * @param existingInfoSystemUuid existing info system UUID
     * @param newInfoSystemUuid new info system UUID
     * @return UUID of created file resource
     */
    @Transactional
    public UUID createFileResourceFromExisting(UUID existingFileUuid, UUID existingInfoSystemUuid, UUID newInfoSystemUuid) {
        FileResource existingFileResource = fileResourceDAO.get(existingFileUuid, existingInfoSystemUuid);

        if (existingFileResource == null) {
            throw new IllegalStateException("FileResource with uuid " + existingFileUuid + " and info system uuid " + existingInfoSystemUuid + " is not found");
        }

        FileResource newFileResource = new FileResource();
        newFileResource.setInfoSystemUuid(newInfoSystemUuid);
        newFileResource.setName(existingFileResource.getName());
        newFileResource.setContentType(existingFileResource.getContentType());
        newFileResource.setLargeObject(existingFileResource.getLargeObject());

        UUID uuid = fileResourceDAO.create(newFileResource);
        logger.info("Created file resource '{}'", uuid);

        return uuid;
    }

    @Transactional
    public void indexFileResource(UUID uuid) {
        try {
            fileResourceIndexingService.indexAsynchronously(uuid);
        } catch (IOException | SQLException e) {
            logger.info("Error indexing file resource '" + uuid + "'", e);
        }
    }

    /**
     * Retrieves single {@link FileResource} by its UUID and info system UUID.
     *
     * @param fileUuid       file resource UUID
     * @param infoSystemUuid UUID of an info system
     * @return file resource or null
     * @see FileResourceDAO#get(UUID, UUID)
     */
    @Transactional
    public FileResource get(UUID fileUuid, UUID infoSystemUuid) {
        return fileResourceDAO.get(fileUuid, infoSystemUuid);
    }

    /**
     * Copies file resource data stream to output stream
     *
     * @param uuid   file resource UUID
     * @param output destination output stream
     * @throws SQLException in case of problems obtaining file resource binary data stream
     * @throws IOException  in case of problems copying file resource data stream to output stream
     */
    @Transactional
    public void copyLargeObjectData(UUID uuid, OutputStream output) throws SQLException, IOException {
        FileResource fileResource = get(uuid);
        if (fileResource == null) {
            throw new IllegalStateException("FileResource with id " + uuid.toString() + " is not found");
        }

        StreamUtils.copy(fileResource.getLargeObject().getData().getBinaryStream(), output);
    }

    /**
     * Retrieves single {@link FileResource} by its UUID.
     *
     * @param fileUuid file resource UUID
     * @return file resource or null
     * @see FileResourceDAO#get(UUID)
     */
    @Transactional
    public FileResource get(UUID fileUuid) {
        return fileResourceDAO.get(fileUuid);
    }

    @Transactional
    public PagedResponse list(PagedRequest request) {
        return registeredFileGrid.query(request);
    }
}
