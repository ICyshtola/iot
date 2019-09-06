package com.icarus.iot.model;

public class DataModel {
    private String record_time;
    private String data;

    public DataModel(String record_time, String data) {
        this.record_time = record_time;
        this.data = data;
    }

    public DataModel() {
    }

    public String getRecord_time() {
        return record_time;
    }

    public void setRecord_time(String record_time) {
        this.record_time = record_time;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
