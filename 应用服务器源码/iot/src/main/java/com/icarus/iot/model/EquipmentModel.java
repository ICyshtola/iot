package com.icarus.iot.model;

public class EquipmentModel {
    private int equipment_id;
    private String name;
    private boolean status;
    private int relay;
    private String mac;
    private int user_id;
    private String open_order;
    private String close_order;
    private String query_order;

    public EquipmentModel() {
    }

    public EquipmentModel(String name, boolean status, int relay, String mac, int user_id, String open_order, String close_order,String query_order) {
        this.name = name;
        this.status = status;
        this.relay = relay;
        this.mac = mac;
        this.user_id = user_id;
        this.open_order = open_order;
        this.close_order = close_order;
        this.query_order = query_order;
    }

    public String getOpen_order() {
        return open_order;
    }

    public String getQuery_order() {
        return query_order;
    }

    public void setQuery_order(String query_order) {
        this.query_order = query_order;
    }

    public void setOpen_order(String open_order) {
        this.open_order = open_order;
    }

    public String getClose_order() {
        return close_order;
    }

    public void setClose_order(String close_order) {
        this.close_order = close_order;
    }

    public int getEquipment_id() {
        return equipment_id;
    }

    public void setEquipment_id(int equipment_id) {
        this.equipment_id = equipment_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getRelay() {
        return relay;
    }

    public void setRelay(int relay) {
        this.relay = relay;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
