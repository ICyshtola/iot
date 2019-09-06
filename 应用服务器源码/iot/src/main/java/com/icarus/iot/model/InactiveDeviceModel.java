package com.icarus.iot.model;

public class InactiveDeviceModel {
    private String serial;
    private String mac;

    public InactiveDeviceModel(String serial, String mac) {
        this.serial = serial;
        this.mac = mac;
    }

    public InactiveDeviceModel() {
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
