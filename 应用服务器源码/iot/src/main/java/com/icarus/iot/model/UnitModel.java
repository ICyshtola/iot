package com.icarus.iot.model;

public class UnitModel {
    private int unit_id;
    private String name;
    private double length;
    private double width;
    private String description;
    private int user_id;

    public UnitModel(String name, double length, double width, String description, int user_id) {
        this.name = name;
        this.length = length;
        this.width = width;
        this.description = description;
        this.user_id = user_id;
    }

    public UnitModel() {
    }

    public int getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
