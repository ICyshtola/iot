package com.icarus.iot.model;

public class ActiveDeviceModel {
    private String mac;
    private String serial;
    private String unit;
    private String sensor;//List to String
    private int user_id;
    private String equipment;//List to String
    private int interval;

    public ActiveDeviceModel(String mac, String serial, int user_id) {
        this.mac = mac;
        this.serial = serial;
        this.user_id = user_id;
    }

    public ActiveDeviceModel() {
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
}
