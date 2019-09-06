package com.icarus.iot.controller;

import com.icarus.iot.mapper.DeviceMapper;
import com.icarus.iot.mapper.UserMapper;
import com.icarus.iot.model.EquipmentModel;
import com.icarus.iot.model.SensorModel;
import com.icarus.iot.tool.DetermineTime;
import com.icarus.iot.tool.TokenManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin
public class DeviceController {
    @Autowired(required = false)
    private DeviceMapper deviceMapper;
    @Autowired(required = false)
    private UserMapper userMapper;

    @PostMapping("/device/addSensor")
    //TODO 判断mini_sensor为json
    public String addSensor(@RequestParam("token") String token,
                            @RequestParam("name") String name,
                            @RequestParam("order") String order,
                            @RequestParam("calculation") String calculation,
                            @RequestParam("mini_sensor") String mini_sensor,
                            @RequestParam("email") String email, HttpSession session) {
        order = "[" + order + "]";
        calculation = "[" + calculation + "]";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "DeviceAddSensor");
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        String content = "";
        int id = 0;
//        if (true) {
        if (varToken.equals(token)) {
            id = userMapper.setFindIdByEmail(email);
            List<SensorModel> sensorModels = deviceMapper.setFindSensorById(id);
            int i;
            for (i = 0; i < sensorModels.size(); i++) {
//                System.out.println(sensorModels.get(i).getSensor_id()+ " " + sensorModels.get(i).getName());
                if (sensorModels.get(i).getName().equals(name)) {
                    content = "repeat";
                    break;
                }
            }
            if (i == sensorModels.size()) {
                SensorModel varSensor = new SensorModel(name, order, calculation, mini_sensor, id);
                deviceMapper.setInsSensor(varSensor);
                userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户添加了传感器: " + name);
                content = "success";
            }
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("token", varToken);
        jsonObject.put("content", content);
        return jsonObject.toString();
    }

    @PostMapping("device/resetSensor")
    //TODO 判断mini_sensor为json
    public String resetSensor(@RequestParam("token") String token,
                              @RequestParam("name") String name,
                              @RequestParam("order") String order,
                              @RequestParam("calculation") String calculation,
                              @RequestParam("mini_sensor") String mini_sensor,
                              @RequestParam("email") String email, HttpSession session) {
        order = "[" + order + "]";
        calculation = "[" + calculation + "]";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "DeviceResetSensor");
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken;
        String content = "";
        int user_id;
//        if(true){
        if (varToken.equals(token)) {
            user_id = userMapper.setFindIdByEmail(email);
//            deviceMapper.setUpdateSensor(order,calculation,mini_sensor,user_id,name);
            SensorModel sensorModel = new SensorModel(name, order, calculation, mini_sensor, user_id);
            deviceMapper.setUpdateSensor(sensorModel);
            userMapper.setInsUserRecord(user_id, DetermineTime.getDateTime(), "用户修改了传感器: " + name + " 的信息");
            content = "success";
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    @PostMapping("/device/delSensor")
    public String delSensor(@RequestParam(name = "token") String token,
                            @RequestParam(name = "name") String name,
                            @RequestParam(name = "email") String email,
                            HttpSession session) {
        String content = "";
        String varToken = "";
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
//            if (true) {
                int user_id = userMapper.setFindIdByEmail(email);
                int sensor_id = deviceMapper.setFindSensoridById(user_id, name);
                Integer sensor_idInterger = sensor_id;
                //将该用户的所有设备的mac读出，并存放到固定长度的list中
                List<String> macList = deviceMapper.setFindMaclistByUserid(user_id);
                //遍历mac列表，查找每个mac的传感器
                for (int i = 0; i < macList.size(); i++) {
                    //找出该设备的所有传感器，并存放到固定长度的list中
                    String sensorStr = deviceMapper.setFindSensorByMac(macList.get(i));
                    sensorStr = sensorStr.substring(1, sensorStr.length() - 1);
                    //但是，如果收到的传感器字段内容为空，就直接跳过下面的步骤
                    if (sensorStr.equals("")) {
                        continue;
                    } else {
                        sensorStr = sensorStr.replace(" ", "");
                        List<String> staticSensorList = Arrays.asList(sensorStr.split(","));
                        //遍历该设备的传感器列表，如果存在，就更新传感器列表，并存入数据库
                        for (int j = 0; j < staticSensorList.size(); j++) {
                            if (staticSensorList.get(j).equals(sensor_idInterger.toString())) {
                                //将固长的list存在变长的可更改的list中
                                List<String> arraySensorList = new ArrayList<>();
                                for (int k = 0; k < staticSensorList.size(); k++) {
                                    arraySensorList.add(staticSensorList.get(k));
                                }
                                arraySensorList.remove(sensor_idInterger.toString());
                                deviceMapper.setUpdateSensorByMac(arraySensorList.toString(), macList.get(i));
                            }
                        }
                    }
                }
                //在sensor表中删除该传感器
                deviceMapper.setDelSensorBySensorid(sensor_id);
                userMapper.setInsUserRecord(user_id, DetermineTime.getDateTime(), "用户删除了传感器: " + name);
                content = "success";
            } else {
                content = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "DeviceDelSensor");
        jsonObject.put("token", varToken);
        jsonObject.put("content", content);
        return jsonObject.toString();
    }

    /**
     * 用户更新设备所属采集单元
     **/
    @PostMapping("/device/resetDeviceUnitandInterval")
    public String resetDeviceUnit(@RequestParam("token") String token,
                                  @RequestParam("mac") String mac,
                                  @RequestParam("unit") String unit,
                                  @RequestParam("email") String email,
                                  @RequestParam("interval") int interval, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken = "";
        String content = "";
        if (varToken.equals(token)) {
            deviceMapper.setResetDeviceUnitandInterval(unit, interval, mac);
            content = "success";
            int id = userMapper.setFindIdByEmail(email);
            userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户更新了设备所属的采集单元为: " + unit + " ,修改了上传数据的间隔为: " + interval + " 秒");
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserResetDeviceUnitandInterval");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    @PostMapping("/device/getInterval")
    public String getInterval(@RequestParam("mac") String mac,
                              @RequestParam("token") String token,
                              HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        String content = "";
        int interval = 0;
        if (varToken.equals(token)) {
            interval = deviceMapper.setFindIntervalByMac(mac);
            content = "success";
        } else {
            content = "tokenError";
        }
        jsonObject.put("msgType", "DeviceGetInterval");
        jsonObject.put("content", interval);
        jsonObject.put("message", content);
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    /**
     * 用户更新设备拥有的传感器
     **/
    @PostMapping("/device/addDeviceSensor")
    public String addDeviceSensor(@RequestParam("token") String token,
                                  @RequestParam("mac") String mac,
                                  @RequestParam("sensor") String sensor,
                                  @RequestParam("email") String email, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        String content = "";

        List<String> sensorList = new ArrayList<>();
        if (varToken.equals(token)) {
            String sensorStr = deviceMapper.setFindSensorByMac(mac);
            sensorStr = sensorStr.substring(1, sensorStr.length() - 1);
            if (!sensorStr.isEmpty()) {
                sensorStr = sensorStr.replace(" ", "");
                List<String> staticSensorList = Arrays.asList(sensorStr.split(","));
                for (int j = 0; j < staticSensorList.size(); j++) {
                    sensorList.add(staticSensorList.get(j));
                }
            }
            sensorList.add(sensor);
            deviceMapper.setResetDeviceSensor(sensorList.toString(), mac);
            int id = userMapper.setFindIdByEmail(email);
            userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户给设备: " + mac + " 添加了传感器: " + sensor);
            content = "success";
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserAddDeviceSensor");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    /**
     * 用户删除设备拥有的传感器
     **/
    @PostMapping("/device/delDeviceSensor")
    public String delDeviceSensor(@RequestParam("token") String token,
                                  @RequestParam("mac") String mac,
                                  @RequestParam("email") String email,
                                  @RequestParam("sensor") String sensor, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        String content = "";
        List<String> sensorList = new ArrayList<>();
        if (varToken.equals(token)) {
            String sensorStr = deviceMapper.setFindSensorByMac(mac);
            sensorStr = sensorStr.substring(1, sensorStr.length() - 1);
            sensorStr = sensorStr.replace(" ", "");
            List<String> staticSensorList = Arrays.asList(sensorStr.split(","));
            for (int j = 0; j < staticSensorList.size(); j++) {
                sensorList.add(staticSensorList.get(j));
            }
            sensorList.remove(sensor);
            deviceMapper.setResetDeviceSensor(sensorList.toString(), mac);
            content = "success";
            int id = userMapper.setFindIdByEmail(email);
            userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户删除了设备: " + mac + " 中的传感器: " + sensor);
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserDelDeviceSensor");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }


    /**
     * 用户更新设备的强电设备
     **/
    @PostMapping("/device/addDeviceEquipment")
    public String addDeviceEquipment(@RequestParam("token") String token,
                                     @RequestParam("mac") String mac,
                                     @RequestParam("name") String name,
                                     @RequestParam("relay") int relay,
                                     @RequestParam("email") String email,
                                     @RequestParam("open_order") String open_order,
                                     @RequestParam("close_order") String close_order,
                                     @RequestParam("query_order") String query_order, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        String content = "";
        if (varToken.equals(token)) {
            int user_id = userMapper.setFindIdByEmail(email);
            List<String> equips = new ArrayList<>();
            //在设备表中添加强电设备名
            String eqp = deviceMapper.setGetEquipmentInDevs(mac);
            eqp = eqp.substring(1, eqp.length() - 1);
            if (!eqp.isEmpty()) {
                eqp = eqp.replace(" ", "");
                List<String> epTables = Arrays.asList(eqp.split(","));
                int i;
                for (i = 0; i < epTables.size(); i++) {
                    equips.add(epTables.get(i));
                    //查看是否重复
                    if (epTables.get(i).equals(name)) {
                        content = "repeat";
                        break;
                    }
                }
                if (i == epTables.size()) {
                    equips.add(name);
                    deviceMapper.setUpdateEquipmentInDevs(equips.toString(), mac);
                    EquipmentModel equipmentModel = new EquipmentModel(name, false, relay, mac, user_id, open_order, close_order, query_order);
                    deviceMapper.setInsEquipment(equipmentModel);
                    content = "success";
                }
            } else {
                equips.add(name);
                deviceMapper.setUpdateEquipmentInDevs(equips.toString(), mac);
                EquipmentModel equipmentModel = new EquipmentModel(name, false, relay, mac, user_id, open_order, close_order, query_order);
                deviceMapper.setInsEquipment(equipmentModel);
                userMapper.setInsUserRecord(user_id, DetermineTime.getDateTime(), "用户给设备: " + mac + " 添加了强电设备: " + name);
                content = "success";
            }
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserAddDeviceEquipment");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }
}
