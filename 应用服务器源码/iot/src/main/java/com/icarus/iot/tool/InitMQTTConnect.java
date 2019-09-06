package com.icarus.iot.tool;

import com.icarus.iot.mqttManager.ClientMQTT;
import com.icarus.iot.mqttManager.ServerMQTT;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class InitMQTTConnect {

    public static InitMQTTConnect initMQTTConnect;

    private ServerMQTT serverMQTT;
    private ClientMQTT clientMQTT;

    @PostConstruct
    public void init() {
        initMQTTConnect = this;
    }

    public void newMQTT() {
        try {
            this.serverMQTT = new ServerMQTT();
            this.clientMQTT = new ClientMQTT();
            clientMQTT.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServerMQTT returnServerMQTT() {
        ServerMQTT serverMQTT = this.serverMQTT;
        return serverMQTT;
    }

    public ClientMQTT returnClientMQTT() {
        ClientMQTT clientMQTT = this.clientMQTT;
        return clientMQTT;
    }
}
