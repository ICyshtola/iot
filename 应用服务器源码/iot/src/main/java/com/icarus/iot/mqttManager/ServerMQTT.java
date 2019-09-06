package com.icarus.iot.mqttManager;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ServerMQTT {
    public static final String host = "tcp://192.168.100.32:1883";
    //public static final String topic = "CONTROL/F4:5E:AB:57:F0:FF/SENSOR/SOIL_TEMP&HUMI";
    public static final String clientId = "server01";

    private MqttClient mqttClient;
    public MqttTopic mqttTopic;
    private String userName = "***";
    private String passWord = "***";

    public MqttMessage message;

    /**
     * 构造函数
     **/
    public ServerMQTT() throws MqttException {
        try {
            mqttClient = new MqttClient(host, clientId, new MemoryPersistence());
            connect();
        } catch (Exception e) {
            System.out.println("fail");
        }
    }

    /**
     * 连接服务器
     **/
    private void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        //options.setUserName(userName);
        //options.setPassword(passWord.toCharArray());

        /**设置超时时间**/
        options.setConnectionTimeout(20);

        /**设置会话心跳时间**/
        options.setKeepAliveInterval(10);

        try {
            mqttClient.setCallback(new PushCallback());
            mqttClient.connect(options);
            //mqttTopic = mqttClient.getTopic(topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置主题
     **/
    public void setTopic(String topic) {
        mqttTopic = mqttClient.getTopic(topic);
    }

    /**
     * @param topic
     * @param message
     * @throws MqttPersistenceException
     * @throws MqttException
     **/
    public void publish(MqttTopic topic, MqttMessage message)
            throws MqttException, MqttPersistenceException {
        MqttDeliveryToken token = topic.publish(message);
        token.waitForCompletion();
        System.out.println("message is published completely! "
                + token.isComplete());
    }

}
