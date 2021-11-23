package ee.eesti.riha.rest.service.impl;

import ee.eesti.riha.rest.service.HealthService;

import ee.eesti.riha.rest.util.HeartBeat;
import ee.eesti.riha.rest.util.HeartBeatInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@PropertySource("classpath:heartbeat.properties")
public class HealthServiceImpl implements HealthService {

    @Value("${app.name}")
    private String appName;
    @Value("${app.version}")
    private String appVersion;
    @Value("${app.packaging.time}")
    private Long appPackagingTime;
    @Resource
    private DataSource dataSource;

    RuntimeMXBean runtimeBeen = ManagementFactory.getRuntimeMXBean();
    Date appStartTime = new Date(runtimeBeen.getStartTime());

    private String getDbStatus() throws SQLException {
        Connection connection = dataSource.getConnection();
        boolean isValid = connection.isValid(5);
        return isValid ? "UP" : "DOWN";
    }

    @Override
    public Response health() throws SQLException {

        HeartBeatInfo heartBeatInfo = new HeartBeatInfo(appName, appVersion, appPackagingTime, appStartTime, LocalDateTime.now().toString(), getDbStatus());
        HeartBeat heartBeat = new HeartBeat(getDbStatus(), heartBeatInfo);

        return Response.ok(heartBeat).build();
    }
}
