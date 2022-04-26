package ee.eesti.riha.rest.dao;

import static java.util.Arrays.asList;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;

@RunWith(Suite.class)
@DataJpaTest
@Suite.SuiteClasses({
        TestApiGenericDAOMain_resource.class,
        TestDatabase.class,
        TestKindRepository.class,
        TestSecureApiGenericDAO.class,
        FileResourceDAOTest.class
})
public class DAOTestSuite {

}
