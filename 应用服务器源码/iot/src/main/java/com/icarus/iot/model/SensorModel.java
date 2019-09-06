package com.icarus.iot.model;

public class SensorModel {

    private int sensor_id;
    private String name;
    private String order;
    private String calculation;
    private String mini_sensor;//List to String
    private int user_id;

    public SensorModel(String name, String order, String calculation, String mini_sensor, int user_id) {
        this.name = name;
        this.order = order;
        this.calculation = calculation;
        this.mini_sensor = mini_sensor;
        this.user_id = user_id;
    }

    public int getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(int sensor_id) {
        this.sensor_id = sensor_id;
    }

    public SensorModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getCalculation() {
        return calculation;
    }

    public void setCalculation(String calculation) {
        this.calculation = calculation;
    }

    public String getMini_sensor() {
        return mini_sensor;
    }

    public void setMini_sensor(String mini_sensor) {
        this.mini_sensor = mini_sensor;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
