package com.wuav.client.be.device;


public class Projector extends Device {

    private String resolution;
    private String connectionType;
    private String devicePort;
    public Projector(int id,String name,String deviceType) {
        super(id, name,deviceType);
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(String devicePort) {
        this.devicePort = devicePort;
    }


    @Override
    public String toString() {
        return getName();
    }
}