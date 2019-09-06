package com.icarus.iot.tool;

import com.icarus.iot.mapper.DeviceMapper;
import com.icarus.iot.mqttManager.ClientMQTT;
import com.icarus.iot.mqttManager.ServerMQTT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MapperUntils {
    @Autowired(required = false)
    public DeviceMapper deviceMapper;

    public static MapperUntils mapperUntils;

    private int status = -1;

    private String order = "";

    @PostConstruct
    public void init() {
        mapperUntils = this;
        mapperUntils.deviceMapper = this.deviceMapper;
    }

    public int getStatus() {
        int status = this.status;
        return status;
    }

    public void changeStatus(int status) {
        this.status = status;
    }

    public String getOrder() {
        String order = this.order;
        return order;
    }

    public void changeOrder(String order) {
        this.order = order;
    }

}