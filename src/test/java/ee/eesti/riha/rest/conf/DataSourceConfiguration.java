package ee.eesti.riha.rest.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DataSourceConfiguration {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init(){
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/create/create_tables.sql"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
