package com.icarus.iot.mqttManager;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ClientMQTT {
    public static final String host = "tcp://192.168.100.32:1883";

    private static final String clientId = "client01";

    private MqttClient client;
    private MqttConnectOptions options;
    private String userName = " *** ";
    private String passWord = " *** ";

    private ScheduledExecutorService scheduler;

    public ClientMQTT() throws MqttException {
        try {


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void reconnect(){
        try{
            client.reconnect();
        }catch (MqttException e){
            e.getCause().printStackTrace();
        }
    }

    public void start() {
        try {
            /**host              主机名
             * clientid          客户端id
             * MemoryPersistence clientid的保存形式，默认是以内存保存
             * **/
            client = new MqttClient(host, clientId, new MemoryPersistence());

            /**MQTT的连接设置**/
            options = new MqttConnectOptions();

            /**设置是否清空session
             * false 服务器保留客户端的连接记录
             * true  每次连接到服务器都以新的身份连接
             * **/
            options.setCleanSession(true);

            /**设置连接的用户名**/
            //options.setUserName(userName);

            /**设置连接的密码**/
            //options.setPassword(passWord.toCharArray());

            /**设置超时时间
             * 单位 s
             * **/
            options.setConnectionTimeout(10);

            /**设置会话心跳时间
             * 服务器每隔 1.5 * n 秒向客户端发送消息判断客户端是否在线
             * 但这个方法没有重连机制
             * 单位 s**/
            options.setKeepAliveInterval(10);

            /**设置回调**/
            client.setCallback(new PushCallback());
            //MqttTopic mqttTopic = client.getTopic(topic);

            /**设置终端的通知消息
             * 判断客户端是否掉线
             * **/
            //options.setWill(mqttTopic, "close".getBytes(), 2, true);

            client.connect();

            /**订阅消息**/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clientSubscribe(String topic) {
        try {
            int[] qos = {1};
            String[] topic1 = {topic};
            client.subscribe(topic1, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
