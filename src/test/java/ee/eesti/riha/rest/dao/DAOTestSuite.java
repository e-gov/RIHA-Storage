package ee.eesti.riha.rest.dao;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestApiGenericDAOMain_resource.class,
        TestDatabase.class,
        TestKindRepository.class,
        TestSecureApiGenericDAO.class,
        FileResourceDAOTest.class
})
public class DAOTestSuite {
    private static final EmbeddedPostgres POSTGRES = new EmbeddedPostgres(V9_6);

    private static String dbHost = "127.0.0.1";
    private static int port = 5438;
    private static String dbName = "riha";
    private static String user = "riha";
    private static String password = "riha";


    @ClassRule
    public static final ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            String url = POSTGRES.start(dbHost, port, dbName, user, password);

            Connection conn = DriverManager.getConnection(url);
            conn.createStatement().execute("CREATE SCHEMA AUTHORIZATION riha;");
            conn.createStatement().execute("CREATE EXTENSION pg_trgm WITH SCHEMA riha;");

            conn.close();
        }

        @Override
        protected void after() {
            POSTGRES.stop();
        }
    };
}
