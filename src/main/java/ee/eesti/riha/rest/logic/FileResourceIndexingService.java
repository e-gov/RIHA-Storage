package ee.eesti.riha.rest.logic;

import ee.eesti.riha.rest.dao.FileResourceDAO;
import ee.eesti.riha.rest.dao.util.ToGsonConverter;
import ee.eesti.riha.rest.model.FileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Provides synchronous and asynchronous methods for creation of {@link FileResource} index content. Searchable index
 * content is created from {@link ee.eesti.riha.rest.model.LargeObject} of {@link FileResource}s.
 */
@Service
public class FileResourceIndexingService {

    private static final Logger logger = LoggerFactory.getLogger(FileResourceIndexingService.class);

    @Autowired
    private List<ToGsonConverter> toGsonConverters;

    @Autowired
    private FileResourceDAO fileResourceDAO;

    /**
     * Shorthand for <strong>asynchronous</strong> method {@link #indexAsynchronously(UUID, boolean)} with reindexing
     * turned off.
     *
     * @param uuid file resource uuid
     */
    @Async
    @Transactional
    public void indexAsynchronously(UUID uuid) throws IOException, SQLException {
        indexAsynchronously(uuid, false);
    }

    /**
     * <strong>Asynchronously</strong> index {@link FileResource} content. Will convert {@link FileResource} data for
     * indexing by database.
     *
     * @param uuid    file resource uuid
     * @param reindex will recreate index if set to true, will not index otherwise
     */
    @Async
    @Transactional
    public void indexAsynchronously(UUID uuid, boolean reindex) throws IOException, SQLException {
        index(uuid, reindex);
    }

    /**
     * <strong>Synchronously</strong> indexes {@link FileResource} content.
     *
     * @param uuid    file resource uuid
     * @param reindex will recreate index if set to true, will not index otherwise
     */
    @Transactional
    public void index(UUID uuid, boolean reindex) throws IOException, SQLException {
        FileResource fileResource = fileResourceDAO.get(uuid);

        if (!reindex && fileResource.getLargeObject().isIndexed()) {
            logger.info("File resource '{}' already indexed, skipping", fileResource.getUuid());
            return;
        }

        logger.info("Starting file resource '{}' indexing", fileResource.getUuid());
        createIndex(fileResource);

        fileResource.getLargeObject().setIndexed(true);
        logger.info("File resource '{}' indexing is complete", fileResource.getUuid());
    }

    /**
     * Mark file as indexed
     *
     * @param uuid file resource uuid
     */
    @Transactional
    public void markAsIndexed(UUID uuid) {
        FileResource fileResource = fileResourceDAO.get(uuid);
        fileResource.getLargeObject().setIndexed(true);
    }

    private void createIndex(FileResource fileResource) throws IOException, SQLException {
        UUID uuid = fileResource.getUuid();

        for (ToGsonConverter converter : toGsonConverters) {
            if (converter.supports(fileResource)) {
                logger.debug("Starting file resource '{}' index creation", uuid);

                fileResource.getLargeObject().setSearchContent(converter.convert(fileResource));

                logger.debug("Index creation for file resource '{}' is complete", uuid);
                return;
            }
        }

        logger.debug("To JSON conversion of file resource '{}' is not supported", uuid);
    }
}
