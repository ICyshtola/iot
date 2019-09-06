package com.icarus.iot.mqttManager;

import com.icarus.iot.tool.DetermineTime;
import com.icarus.iot.tool.InitMQTTConnect;
import com.icarus.iot.tool.SeverMqttStart;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import static com.icarus.iot.tool.MapperUntils.mapperUntils;


public class PushCallback implements MqttCallback {

    public void connectionLost(Throwable cause) {
        try {
            InitMQTTConnect.initMQTTConnect.returnClientMQTT().reconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**连接丢失后，在此重连**/
        System.out.println("连接断开，已重连");
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("deliveryComplete : " + token.isComplete());
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println(topic);
        List<String> topicList = Arrays.asList(topic.split("/"));
        String type = topicList.get(2);

        if (topicList.get(0).equals("UPLOAD")) {

            /**根据type不同，执行不同任务**/
            if (type.equals("SENSOR_DATA")) {

                /**Mac**/
                String Mac = "";
                /**数据**/
                JSONArray jsonArray = new JSONArray();
                /**当前时间**/
                String dataTime = "";

                /**subscribe后得到的消息会执行到这里**/
                System.out.println("接收消息主题 : " + topic);
                System.out.println("接收消息     : " + message.getQos());
                byte[] data = message.getPayload();
                System.out.println("接收消息内容 : ");

                /**每4个字节为一个数据，dataNumber表示有多少数据**/
                int iMax = 0;
                int dataNumber = data.length / 8;
                double[] doubleData = new double[dataNumber];

                iMax = dataNumber;
                for (int i = 0; i < iMax; i++) {
                    doubleData[i] = SeverMqttStart.byteToDouble(data, i * 8);
                    System.out.println(doubleData[i]);
                }

                Mac = topicList.get(1);
                List<String> sensorList = Arrays.asList(topicList.get(3).split("_"));

                /**将传感器名、数据和单位放入jsonobject,然后放入json数组**/
                iMax = sensorList.size();
                String sensorName = "";
                String sensorUnit = "";
                for (int i = 0; i < iMax; i++) {
                    sensorName = sensorList.get(i).split(":")[0];
                    sensorUnit = sensorList.get(i).split(":")[1];

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sensorName", sensorName);
                    jsonObject.put("data", doubleData[i]);
                    jsonObject.put("sensorUnit", sensorUnit);

                    jsonArray.put(jsonObject);
                }
                /**获取当前时间**/
                dataTime = DetermineTime.getDateTime();
                /**将数据和当前时间存入数据库**/
                mapperUntils.deviceMapper.setInsertDataInMac(Mac, jsonArray.toString(), dataTime);
                System.out.println("已存入");
            } else if (type.equals("RELAY_CONTROL")) {
                /**subscribe后得到的消息会执行到这里**/
                System.out.println("接收消息主题 : " + topic);
                System.out.println("接收消息     : " + message.getQos());
                System.out.print("接收消息内容 : ");

                byte[] data = message.getPayload();
                System.out.println(Arrays.toString(data));
                int relay = Integer.parseInt(topicList.get(3));
                int count = relay / 8;
                int shift = relay % 8 - 1;
                int value = 1;
                value = (value << shift) & data[count];

                mapperUntils.changeStatus(value);

            } else if (type.equals("LOCAL_CONTROL")) {
                String mac = topicList.get(1);
                List<String> equipmentNameList = Arrays.asList(topicList.get(3).split("_"));
                List<String> equipmentRelayList = Arrays.asList(topicList.get(4).split("_"));

                byte[] data = message.getPayload();
                int iMax = -1;
                int relay = -1, count = -1, shift = -1;
                int value = 1;
                iMax = equipmentRelayList.size();
                for (int i = 0; i < iMax; i++) {
                    value = 1;
                    relay = Integer.parseInt(equipmentRelayList.get(i));
                    count = relay / 8;
                    shift = relay % 8 - 1;
                    value = (value << shift) & data[2 * i + count];
                    mapperUntils.deviceMapper.setUpdateStatusByMacandEquipmentname(value, equipmentNameList.get(i), mac);
                    System.out.println("已更新强电设备状态");
                }
            } else if (type.equals("DEBUG")) {
                byte[] data = message.getPayload();
                mapperUntils.changeOrder(new String(data));
                System.out.println("PushCall: " + new String(data));
            } else {
                /**不做任何操作**/
            }

        }
    }
}
