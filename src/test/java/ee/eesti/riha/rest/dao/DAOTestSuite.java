package ee.eesti.riha.rest.dao;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.beans.factory.annotation.Value;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.sql.Connection;
import java.sql.DriverManager;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestApiGenericDAOMain_resource.class,
        TestApiGenericDAONames.class,
        TestDatabase.class,
        TestKindRepository.class,
        TestNamesDAO.class,
        TestSecureApiGenericDAO.class
})
public class DAOTestSuite {
    private static final EmbeddedPostgres POSTGRES = new EmbeddedPostgres(V9_6);
    ;

    @ClassRule
    public static final ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            String url = POSTGRES.start("localhost", 5432, "riha", "riha", "riha");

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
