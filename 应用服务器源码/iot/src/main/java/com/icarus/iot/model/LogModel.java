package com.icarus.iot.model;

public class LogModel {
    private String record_time;
    private String behavior;

    public LogModel() {
    }

    public LogModel(String record_time, String behavior) {
        this.record_time = record_time;
        this.behavior = behavior;
    }

    public String getRecord_time() {
        return record_time;
    }

    public void setRecord_time(String record_time) {
        this.record_time = record_time;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }
}
