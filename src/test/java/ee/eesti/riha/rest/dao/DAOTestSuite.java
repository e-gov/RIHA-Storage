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
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestApiGenericDAOMain_resource.class,
        TestDatabase.class,
        TestKindRepository.class,
        TestSecureApiGenericDAO.class,
        FileResourceDAOTest.class
})
public class DAOTestSuite {
    private static final List<String> DEFAULT_ADD_PARAMS = asList(
        "-E", "SQL_ASCII",
        "--locale=C",
        "--lc-collate=C",
        "--lc-ctype=C");

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
            String url = POSTGRES.start(getPostgresRuntimeConfig(), dbHost, port, dbName, user, password, DEFAULT_ADD_PARAMS);

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

    public static IRuntimeConfig getPostgresRuntimeConfig() {
        return new RuntimeConfigBuilder()
            .defaults(Command.Postgres)
            .artifactStore(new PostgresArtifactStoreBuilder()
                .defaults(Command.Postgres)
                .download(new PostgresDownloadConfigBuilder()
                    .defaultsForCommand(Command.Postgres)
                    .downloadPath("https://nexus.riaint.ee/repository/raw-public/postgresql/")
                    .build()))
            .commandLinePostProcessor(privilegedWindowsRunasPostprocessor())
            .build();
    }

    private static ICommandLinePostProcessor privilegedWindowsRunasPostprocessor() {
        if (Platform.detect().equals(Platform.Windows)) {
            try {
                // Based on https://stackoverflow.com/a/11995662
                final int adminCommandResult = Runtime.getRuntime().exec("net session").waitFor();
                if (adminCommandResult == 0) {
                    return runWithoutPrivileges();
                }
            } catch (Exception e) {
                // Log maybe?
            }
        }
        return doNothing();
    }

    private static ICommandLinePostProcessor runWithoutPrivileges() {
        return (distribution, args) -> {
            if (args.size() > 0 && args.get(0).endsWith("postgres.exe")) {
                return Arrays.asList("runas", "/trustlevel:0x20000", String.format("\"%s\"", String.join(" ", args)));
            }
            return args;
        };
    }

    private static ICommandLinePostProcessor doNothing() {
        return (distribution, args) -> args;
    }
}
