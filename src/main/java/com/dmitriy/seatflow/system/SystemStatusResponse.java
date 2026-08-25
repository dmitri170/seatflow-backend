package com.dmitriy.seatflow.system;

public class SystemStatusResponse {

    private final String status;
    private final String service;

    public SystemStatusResponse(String status, String service) {
        this.status = status;
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public String getService() {
        return service;
    }
}
