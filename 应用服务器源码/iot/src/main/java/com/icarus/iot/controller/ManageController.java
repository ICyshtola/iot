package com.icarus.iot.controller;

import com.icarus.iot.mapper.DeviceMapper;
import com.icarus.iot.mapper.UserMapper;
import com.icarus.iot.model.EquipmentModel;
import com.icarus.iot.model.MonitorModel;
import com.icarus.iot.model.SensorModel;
import com.icarus.iot.mqttManager.ClientMQTT;
import com.icarus.iot.mqttManager.ServerMQTT;
import com.icarus.iot.tool.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin
public class ManageController {
    @Autowired(required = false)
    private UserMapper userMapper;
    @Autowired(required = false)
    private DeviceMapper deviceMapper;

    @PostMapping("/user/sendSensorOrder")
    public String sendSensorOrder(@RequestParam(name = "token") String token,
                                  @RequestParam(name = "email") String email,
                                  @RequestParam(name = "mac") String mac,
                                  HttpSession session) {
        String varToken = "";
        String content = "";
        int interval = 3;
        try {
            //发送间隔指令
            interval = deviceMapper.setFindIntervalByMac(mac);
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
//            if (true) {
            if (token.equals(varToken)) {
                int userId = userMapper.setFindIdByEmail(email);

                String sensorInADeviceStr = deviceMapper.setFindSensorByIdandMac(userId, mac);
                sensorInADeviceStr = sensorInADeviceStr.substring(1, sensorInADeviceStr.length() - 1);

                if (!sensorInADeviceStr.equals("")) {

                    sensorInADeviceStr = sensorInADeviceStr.replace(" ", "");
                    List<String> sensorInADevice = Arrays.asList(sensorInADeviceStr.split(","));

                    byte[] totalOrder = new byte[sensorInADevice.size() * 8];
                    byte[] totalCalculation = new byte[sensorInADevice.size() * 20];
                    byte[] totalDataNum = new byte[sensorInADevice.size() * 4];
                    int counter = 0;
                    String topic = "";
                    String totalCalculationStr = "";
                    int iMax = sensorInADevice.size();
                    int jMax;
                    int kMax;
                    /**遍历一个设备的所有传感器，拼接命令字符串和最小传感器名字符串**/
                    for (int i = 0; i < iMax; i++) {
                        SensorModel sensor = deviceMapper.setFindSensorByNameandUserid(sensorInADevice.get(i), userId);

                        String minisensorStr = sensor.getMini_sensor();
                        /**目前mini_sensor为json数组字符串**/
                        minisensorStr = minisensorStr.replace("\\", "");
                        minisensorStr = minisensorStr.replace("\"{", "{");
                        minisensorStr = minisensorStr.replace("}\"", "}");
                        JSONArray jsonArray = new JSONArray(minisensorStr);

                        jMax = jsonArray.length();
                        for (int j = 0; j < jMax; j++) {
                            JSONObject aMinisensor = jsonArray.getJSONObject(j);
                            topic += aMinisensor.get("name") + ":" + aMinisensor.get("unit") + "_";
                        }
                        /**将单个传感器拥有的传感器数量转成byte[]**/
                        byte[] b = SeverMqttStart.intToByte(jMax);
                        totalDataNum[i * 4 + 0] = b[0];
                        totalDataNum[i * 4 + 1] = b[1];
                        totalDataNum[i * 4 + 2] = b[2];
                        totalDataNum[i * 4 + 3] = b[3];

                        /**此代码块假设mini_sensor为json字符串**/
//                        JSONObject minisensorJson = JSONObject.fromObject(minisensorStr);
//
//                        Iterator<String> iterator = minisensorJson.keys();
//
//                        String key;
//                        String value;
//                        while(iterator.hasNext()){
//                            key = iterator.next();
//                            value = minisensorJson.getString(key);
//                            topic += key + ":" + value + "_";
//                        }

                        /**此代码块假设mini_sensor为listtoString字符串**/
//                        minisensorStr = minisensorStr.substring(1, minisensorStr.length() - 1);
//                        List<String> minisensorList = Arrays.asList(minisensorStr.split(", "));
//                        jMax = minisensorList.size();
//
//                        /**进行主题拼接**/
//                        for ( int j = 0; j < jMax; j++ ) {
//                            topic += minisensorList.get(j);
//                            if (i < iMax - 1 || j < jMax - 1) {
//                                topic += "_";
//                            }
//                            else {
//                                /**不做任何操作**/
//                            }
//                        }

                        String orderStr = sensor.getOrder();
                        orderStr = orderStr.substring(1, orderStr.length() - 1);

                        orderStr = orderStr.replace(" ", "");
                        List<String> orderListStr = Arrays.asList(orderStr.split(","));

                        /**进行命令拼接**/
                        jMax = orderListStr.size();
                        for (int j = 0; j < jMax; j++) {
                            totalOrder[counter] = SeverMqttStart.strToByte(orderListStr.get(j))[0];
                            counter++;
                        }

                        String calculationStr = sensor.getCalculation();
                        calculationStr = calculationStr.substring(1, calculationStr.length() - 1);

                        calculationStr = calculationStr.replace(" ", "");
                        List<String> calculationList = Arrays.asList(calculationStr.split(","));

                        /**进行计算方式拼接**/
                        jMax = calculationList.size();
                        for (int j = 0; j < jMax; j++) {
                            totalCalculationStr += calculationList.get(j);
                            totalCalculationStr += "_";
                        }
                        char[] totalCalculationChar = totalCalculationStr.toCharArray();
                        totalCalculation = SeverMqttStart.charToByte(totalCalculationChar, totalCalculationChar.length);
                    }

                    /**命令和主题已拼接完毕，可以发布并订阅**/
                    ServerMQTT serverMQTT = InitMQTTConnect.initMQTTConnect.returnServerMQTT();
                    ClientMQTT clientMQTT = InitMQTTConnect.initMQTTConnect.returnClientMQTT();
                    String irtopic = "CONTROL/" + mac + "/SENSOR_CYCLE";
                    byte[] internalData = SeverMqttStart.intToByte(interval);
                    SeverMqttStart.start(serverMQTT, internalData, irtopic);

                    String clientTopic = "UPLOAD/" + mac + "/SENSOR_DATA/" + topic;
                    clientMQTT.clientSubscribe(clientTopic);

                    String sensorDataNumTopic = "CONTROL/" + mac + "/SENSOR_DATA_COUNT";
                    SeverMqttStart.start(serverMQTT, totalDataNum, sensorDataNumTopic);
                    String calculationTopic = "CONTROL/" + mac + "/SENSOR_CAL/" + topic;
                    SeverMqttStart.start(serverMQTT, totalCalculation, calculationTopic);
                    String serverTopic = "CONTROL/" + mac + "/SENSOR_DATA/" + topic;
                    SeverMqttStart.start(serverMQTT, totalOrder, serverTopic);
                    MonitorModel monitorModel;
                    List<String> sensorNameandUnit = Arrays.asList(topic.split("_"));
                    int sensorNameandUnitSize = sensorNameandUnit.size();
                    for (int i = 0; i < iMax; i++) {
                        List<String> allMinisensor = deviceMapper.setFindAllMinisensornameInMonitorByMacandSensorname(mac, sensorInADevice.get(i));

                        if (allMinisensor.size() == 0) {
                            continue;
                        }

                        jMax = allMinisensor.size();
                        for (int j = 0; j < jMax; j++) {
                            /**获取到一整条的信息**/
                            monitorModel = deviceMapper.setFindMinisensorInMonitor(mac, sensorInADevice.get(i), allMinisensor.get(j));

                            /**获取该小传感器是第几个数据**/
                            int No = -1;
                            for (int k = 0; k < sensorNameandUnitSize; k++) {
                                System.out.println("sensorName:" + Arrays.asList(sensorNameandUnit.get(k).split(":")).get(0));
                                if (allMinisensor.get(j).equals(Arrays.asList(sensorNameandUnit.get(k).split(":")).get(0))) {
                                    No = k;
                                    k = sensorNameandUnitSize;
                                }
                            }

                            if (No == -1) {
                                System.out.println(No);
                                continue;
                            }

                            String minOrderStr = monitorModel.getMin_order();
                            minOrderStr = minOrderStr.substring(1, minOrderStr.length() - 1);
                            String maxOrderStr = monitorModel.getMax_order();
                            maxOrderStr = maxOrderStr.substring(1, maxOrderStr.length() - 1);
                            String equipmentNameStr = monitorModel.getEquipment_name();
                            equipmentNameStr = equipmentNameStr.substring(1, equipmentNameStr.length() - 1);
                            List<String> minOrder = Arrays.asList(minOrderStr.replace(" ", "").split(","));
                            List<String> maxOrder = Arrays.asList(maxOrderStr.replace(" ", "").split(","));
                            List<String> equipmentName = Arrays.asList(equipmentNameStr.replace(" ", "").split(","));

                            /**取出消息内容**/
                            byte[] noByte = SeverMqttStart.intToByte(No);
                            byte[] minByte = SeverMqttStart.doubleToByte(monitorModel.getMin());
                            byte[] minOrderNumByte = SeverMqttStart.intToByte(minOrder.size() / 8);
                            byte[] minOrderByte = new byte[minOrder.size()];
                            kMax = minOrder.size();
                            for (int k = 0; k < kMax; k++) {
                                minOrderByte[k] = SeverMqttStart.strToByte(minOrder.get(k))[0];
                            }
                            byte[] maxByte = SeverMqttStart.doubleToByte(monitorModel.getMax());
                            byte[] maxOrderNumByte = SeverMqttStart.intToByte(maxOrder.size() / 8);
                            byte[] maxOrderByte = new byte[maxOrder.size()];
                            kMax = maxOrder.size();
                            for (int k = 0; k < kMax; k++) {
                                maxOrderByte[k] = SeverMqttStart.strToByte(maxOrder.get(k))[0];
                            }
                            byte[] queryOrderNumByte = SeverMqttStart.intToByte(equipmentName.size());
                            byte[] queryOrderByte = new byte[equipmentName.size() * 8];
                            int queryOrderByteCounter = 0;
                            kMax = equipmentName.size();
                            String queryOrderInAEquipment = "";
                            String equipmentRelay = "";
                            String equipmentNameInMonitor = "";
                            EquipmentModel equipmentModel;
                            for (int k = 0; k < kMax; k++) {
                                equipmentModel = deviceMapper.setFindEquipmentInequipment(equipmentName.get(k), mac, userId);
                                queryOrderInAEquipment = equipmentModel.getQuery_order();
                                equipmentNameInMonitor += equipmentModel.getName() + "_";
                                equipmentRelay += String.valueOf(equipmentModel.getRelay()) + "_";
                                queryOrderInAEquipment = queryOrderInAEquipment.substring(1, queryOrderInAEquipment.length() - 1);
                                List<String> queryOrder = Arrays.asList(queryOrderInAEquipment.replace(" ", "").split(","));
                                for (int t = 0; t < 8; t++) {
                                    queryOrderByte[queryOrderByteCounter] = SeverMqttStart.strToByte(queryOrder.get(t))[0];
                                    queryOrderByteCounter++;
                                }
                            }

                            /**存入消息**/
                            int minOrderSize = minOrder.size();
                            int maxOrderSize = maxOrder.size();
                            int equipmentNameSize = equipmentName.size();
                            kMax = 32 + minOrderSize + maxOrderSize + equipmentNameSize * 8;
                            byte[] localControlData = new byte[kMax];
                            for (int k = 0; k < kMax; k++) {
                                if (k < 4) {
                                    localControlData[k] = noByte[k];
                                } else if (k < 12) {
                                    localControlData[k] = minByte[k - 4];
                                } else if (k < 16) {
                                    localControlData[k] = minOrderNumByte[k - 12];
                                } else if (k < (16 + minOrderSize)) {
                                    localControlData[k] = minOrderByte[k - 16];
                                } else if (k < (24 + minOrderSize)) {
                                    localControlData[k] = maxByte[k - 16 - minOrderSize];
                                } else if (k < (28 + minOrderSize)) {
                                    localControlData[k] = maxOrderNumByte[k - 24 - minOrderSize];
                                } else if (k < (28 + minOrderSize + maxOrderSize)) {
                                    localControlData[k] = maxOrderByte[k - 28 - minOrderSize];
                                } else if (k < (32 + minOrderSize + maxOrderSize)) {
                                    localControlData[k] = queryOrderNumByte[k - 28 - minOrderSize - maxOrderSize];
                                } else {
                                    localControlData[k] = queryOrderByte[k - 32 - minOrderSize - maxOrderSize];
                                }
                            }
                            /**准备订阅和发布**/
                            String clientLocalControlTopic = "UPLOAD/" + mac + "/LOCAL_CONTROL/" + equipmentNameInMonitor + "/" + equipmentRelay;
                            clientMQTT.clientSubscribe(clientLocalControlTopic);
                            String serverLocalControlTopic = "CONTROL/" + mac + "/LOCAL_CONTROL/" + equipmentNameInMonitor + "/" + equipmentRelay;
                            SeverMqttStart.start(serverMQTT, localControlData, serverLocalControlTopic);
                        }
                    }
                    userMapper.setInsUserRecord(userId, DetermineTime.getDateTime(), "用户重新请求设备: " + mac + " 发送数据");
                    content = "success";
                } else {
                    content = "noSensor";
                }
            } else {
                content = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserSendSensorOrder");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);

        return jsonObject.toString();
    }

    @PostMapping("/user/controlEquipment")
    public String controlEquipment(@RequestParam(name = "token") String token,
                                   @RequestParam(name = "mac") String mac,
                                   @RequestParam(name = "email") String email,
                                   @RequestParam(name = "name") String equipmentName,
                                   @RequestParam(name = "status") boolean equipmentStatus,
                                   HttpSession session) {
        String varToken = "";
        String content = "";
        String orderStr = "";

        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
//            if (true){
            if (token.equals(varToken)) {
                int userId = userMapper.setFindIdByEmail(email);

                /**查询强电设备的一条记录**/
                EquipmentModel equipmentModel = deviceMapper.setFindEquipmentInequipment(equipmentName, mac, userId);
                String queryorderStr = equipmentModel.getQuery_order();
                queryorderStr = queryorderStr.substring(1, queryorderStr.length() - 1);
                queryorderStr = queryorderStr.replace(" ", "");
                List<String> queryorderList = Arrays.asList(queryorderStr.split(","));

                byte[] order = new byte[16];
                int iMax;

                if (!equipmentStatus) {
                    orderStr = equipmentModel.getClose_order();
                } else {
                    orderStr = equipmentModel.getOpen_order();
                }

                orderStr = orderStr.substring(1, orderStr.length() - 1);
                orderStr = orderStr.replace(" ", "");

                List<String> orderList = Arrays.asList(orderStr.split(","));

                iMax = orderList.size();
                for (int i = 0; i < iMax; i++) {
                    order[i] = SeverMqttStart.strToByte(orderList.get(i))[0];
                    order[i + 8] = SeverMqttStart.strToByte(queryorderList.get(i))[0];
                }

                MapperUntils.mapperUntils.changeStatus(-1);

                ServerMQTT serverMQTT = InitMQTTConnect.initMQTTConnect.returnServerMQTT();
                ClientMQTT clientMQTT = InitMQTTConnect.initMQTTConnect.returnClientMQTT();
                String clientTopic = "UPLOAD/" + mac + "/RELAY_CONTROL/" + equipmentModel.getRelay();
                clientMQTT.clientSubscribe(clientTopic);
                String serverTopic = "CONTROL/" + mac + "/RELAY_CONTROL/" + equipmentModel.getRelay();
                SeverMqttStart.start(serverMQTT, order, serverTopic);

                Thread.sleep(1000 * 2);

                /**查看强电设备真实状态**/
                int status = MapperUntils.mapperUntils.getStatus();
                if (!equipmentStatus) {
                    if (status == 0) {
                        content = "close";
                    } else if (status == 1) {
                        content = "closeFail";
                    } else {
                        content = "deviceNoResponse";
                    }
                } else {
                    if (status == 0) {
                        content = "openFail";
                    } else if (status == 1) {
                        content = "open";
                    } else {
                        content = "deviceNoResponse";
                    }
                }

                if (status == 0) {
                    equipmentStatus = false;
                } else if (status == 1) {
                    equipmentStatus = true;
                }
                deviceMapper.setUpdateStatusInEquipment(equipmentStatus, equipmentName, mac, userId);
                userMapper.setInsUserRecord(userId, DetermineTime.getDateTime(), "控制强电设备" + equipmentName + "的开关");
            } else {
                content = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserControlEquipment");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);

        return jsonObject.toString();
    }

    @PostMapping("/user/setLocalControl")
    public String setLocalControl(@RequestParam(name = "token") String token,
                                  @RequestParam(name = "mac") String mac,
                                  @RequestParam(name = "email") String email,
                                  @RequestParam(name = "sensorName") String sensorName,
                                  @RequestParam(name = "minisensorName") String minisensorName,
                                  @RequestParam(name = "max") double max,
                                  @RequestParam(name = "min") double min,
                                  @RequestParam(name = "maxOrder") String maxOrder,
                                  @RequestParam(name = "minOrder") String minOrder,
                                  @RequestParam(name = "equipmentName") String equipmentName,
                                  HttpSession session) {
        String varToken = "";
        String content = "";
        equipmentName = equipmentName.replace("\"","");
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
                int userId = userMapper.setFindIdByEmail(email);
                String minisensorNameInDb = deviceMapper.setFindMinisensornameInMonitor(sensorName, minisensorName, mac);

                /**不存在就插入记录，存在就更新记录**/
                MonitorModel monitorModel = new MonitorModel(minisensorName, sensorName, max, min, minOrder, maxOrder, equipmentName, mac);
                if (minisensorNameInDb == null) {
                    deviceMapper.setInsertMonitor(monitorModel);

                    content = "insertSuccess";
                } else {
                    deviceMapper.setUpdateMonitor(monitorModel);
                    content = "updateSuccess";
                }
                userMapper.setInsUserRecord(userId, DetermineTime.getDateTime(), "用户为传感器:" + sensorName + "中的最小传感器" + minisensorName + "添加了本地控制");
            } else {
                content = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserSetLocalControl");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }


    @PostMapping("/user/transOrder")
    public String transOrder(@RequestParam(name = "token") String token,
                             @RequestParam(name = "order") String order,
                             @RequestParam(name = "mac") String mac,
                             HttpSession session) {
        String varToken = "";
        String content = "";
        System.out.println("transOrder: " + order + mac);
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
                ServerMQTT serverMQTT = InitMQTTConnect.initMQTTConnect.returnServerMQTT();
                ClientMQTT clientMQTT = InitMQTTConnect.initMQTTConnect.returnClientMQTT();

                order = order.replace(" ", "");
                List<String> orderList = Arrays.asList(order.split(","));
                int iMax = orderList.size();
                byte[] orderByte = new byte[iMax];
                for (int i = 0; i < iMax; i++) {
                    orderByte[i] = SeverMqttStart.strToByte(orderList.get(i))[0];
                }
                String transOrderClientTopic = "UPLOAD/" + mac + "/DEBUG";
                clientMQTT.clientSubscribe(transOrderClientTopic);
                String transOrderServerTopic = "CONTROL/" + mac + "/DEBUG";
                SeverMqttStart.start(serverMQTT, orderByte, transOrderServerTopic);
                MapperUntils.mapperUntils.changeOrder("");
                Thread.sleep(1000 * 3);
                String newOrder = MapperUntils.mapperUntils.getOrder();
                content = newOrder;
            } else {
                content = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserTransOrder");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }
}
