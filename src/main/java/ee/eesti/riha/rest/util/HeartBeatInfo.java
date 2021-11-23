package ee.eesti.riha.rest.util;

import java.util.Date;

public class HeartBeatInfo {
    private String appName;
    private String version;
    private long packagingTime;
    private Date appStartTime;
    private String serverTime;
    private String dbConnection;

    public HeartBeatInfo(String appName, String version, long packagingTime, Date appStartTime, String serverTime, String dbConnection) {
        this.appName = appName;
        this.version = version;
        this.packagingTime = packagingTime;
        this.appStartTime = appStartTime;
        this.serverTime = serverTime;
        this.dbConnection = dbConnection;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getPackagingTime() {
        return packagingTime;
    }

    public void setPackagingTime(long packagingTime) {
        this.packagingTime = packagingTime;
    }

    public Date getAppStartTime() {
        return appStartTime;
    }

    public void setAppStartTime(Date appStartTime) {
        this.appStartTime = appStartTime;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public String getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(String dbConnection) {
        this.dbConnection = dbConnection;
    }
}
