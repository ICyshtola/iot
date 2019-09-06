package com.icarus.iot.model;

public class MonitorModel {
    private int id;
    private String mini_sensor_name;
    private String sensor_name;
    private double max;
    private double min;
    private String min_order;       /**listtostring**/
    private String max_order;       /**listtostring**/
    private String equipment_name;  /**listtostring**/
    private String mac;

    public MonitorModel() {
    }

    public MonitorModel(String mini_sensor_name, String sensor_name, double max, double min, String min_order, String max_order, String equipment_name, String mac) {
        this.mini_sensor_name = mini_sensor_name;
        this.sensor_name = sensor_name;
        this.max = max;
        this.min = min;
        this.min_order = min_order;
        this.max_order = max_order;
        this.equipment_name = equipment_name;
        this.mac = mac;
    }

    public String getSensor_name() {
        return sensor_name;
    }

    public void setSensor_name(String sensor_name) {
        this.sensor_name = sensor_name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMini_sensor_name() {
        return mini_sensor_name;
    }

    public void setMini_sensor_name(String mini_sensor_name) {
        this.mini_sensor_name = mini_sensor_name;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public String getMin_order() {
        return min_order;
    }

    public void setMin_order(String min_order) {
        this.min_order = min_order;
    }

    public String getMax_order() {
        return max_order;
    }

    public void setMax_order(String max_order) {
        this.max_order = max_order;
    }

    public String getEquipment_name() {
        return equipment_name;
    }

    public void setEquipment_name(String equipment_name) {
        this.equipment_name = equipment_name;
    }
}
