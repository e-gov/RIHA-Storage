package ee.eesti.riha.rest.dao;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestApiGenericDAOMainResource.class,
        TestDatabase.class,
        TestKindRepository.class,
        TestSecureApiGenericDAO.class,
        FileResourceDAOTest.class
})
public class DAOTestSuite {

    private static String PROJECT_DB = "riha";
    private static String POSTGRES_IMAGE = "nexus.riaint.ee:8500/postgres:12";

    static DockerImageName myImage = DockerImageName.parse(POSTGRES_IMAGE).asCompatibleSubstituteFor("postgres");

    @ClassRule
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(myImage)
            .withDatabaseName(PROJECT_DB)
            .withUsername(PROJECT_DB)
            .withPassword(PROJECT_DB);

    @BeforeClass
    public static void setUp() throws IOException {

        System.setProperty("riharest.jdbc.url", postgres.getJdbcUrl());
        System.setProperty("riharest.jdbc.user", postgres.getUsername());
        System.setProperty("riharest.jdbc.password", postgres.getPassword());
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("tearing down");
    }
}
