package ee.eesti.riha.rest.logic;

import ee.eesti.riha.rest.dao.FileResourceDAO;
import ee.eesti.riha.rest.model.FileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * <p>Scheduled job for {@link FileResource} indexing. Will create index for {@link FileResource} {@link
 * ee.eesti.riha.rest.model.LargeObject}s that are not yet indexed.</p>
 */
@Component
public class FileResourceIndexingJob {

    private static final Logger logger = LoggerFactory.getLogger(FileResourceIndexingJob.class);

    @Autowired
    private FileResourceDAO fileResourceDAO;

    @Autowired
    private FileResourceIndexingService fileResourceIndexingService;

    @Value("${riharest.job.fileResourceIndexing.enabled}")
    private boolean enabled;

    /**
     * Retrieves list of {@link FileResource}s that were not indexed and creates index for them.
     */
    @Scheduled(cron = "${riharest.job.fileResourceIndexing.cron}")
    public void indexUnindexedFiles() {
        if (!enabled) {
            logger.debug("File resource indexing job is disabled");
            return;
        }

        logger.info("Starting file resource indexing job");

        List<FileResource> unindexedFileResources = fileResourceDAO.getUnindexedFiles();
        for (FileResource fileResource : unindexedFileResources) {
            try {
                fileResourceIndexingService.index(fileResource.getUuid(), false);
            } catch (IOException | SQLException e) {
                logger.info("Error indexing file resource '" + fileResource.getUuid() + "'", e);
            }
        }

        logger.info("File resource indexing job is finished");
    }
}
