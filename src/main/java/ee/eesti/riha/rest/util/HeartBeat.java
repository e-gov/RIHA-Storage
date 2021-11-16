package ee.eesti.riha.rest.util;

public class HeartBeat {
    private String status;
    private HeartBeatInfo details;

    public HeartBeat(String status, HeartBeatInfo details) {
        this.status = status;
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HeartBeatInfo getDetails() {
        return details;
    }

    public void setDetails(HeartBeatInfo details) {
        this.details = details;
    }
}
