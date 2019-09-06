package com.icarus.iot.controller;

import com.icarus.iot.mapper.DeviceMapper;
import com.icarus.iot.mapper.UserMapper;
import com.icarus.iot.model.*;
import com.icarus.iot.tool.DetermineTime;
import com.icarus.iot.tool.EmailManager;
import com.icarus.iot.tool.RandomUtils;
import com.icarus.iot.tool.TokenManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin
public class UserController {

    @Autowired(required = false)
    private UserMapper userMapper;
    @Autowired(required = false)
    private DeviceMapper deviceMapper;

    @PostMapping("/quest/code")
    public void sendCode(@RequestParam("email") String email,
                         @RequestParam("type") String type,
                         HttpServletRequest request) {
        String code = EmailManager.achieveCode();
        request.getSession().removeAttribute(type);
        request.getSession().setAttribute(type, code);
        EmailManager.sendAuthCodeEmail(email, code);
        System.out.println("send email success " + type + " " + request.getSession().getAttribute(type).toString());
        System.out.println("code sessionId: " + request.getSession().getId());
        EmailManager.removeAttrbute(request.getSession(), type);
    }

    @PostMapping("/user/register")
    public String userRegister(@RequestParam("name") String name,
                               @RequestParam("password") String password,
                               @RequestParam("avatar") String avatar,
                               @RequestParam("email") String email,
                               @RequestParam("code") String code,
                               @RequestParam("type") String type,
                               HttpSession session) {
        System.out.println("register " + name + " " + password + " " + email + " " + code + " " + type);
        String content = "";
        System.out.println("register sessionId: " + session.getId());
        Object o = session.getAttribute(type);
        String registerCode = "";
        if (o == null) {
            System.out.println("session中没有值");
            registerCode = "****";
        } else {
            registerCode = (String) o;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserRegister");
        if (code.equalsIgnoreCase(registerCode)) {
            session.removeAttribute(type);
            User temp = userMapper.setFindUserByEmail(email);
            if (temp == null) {
                String now_time = DetermineTime.getDateTime();
                User user = new User(name, password, avatar, email, now_time, now_time);
                userMapper.setInsert_user(user);
                int id = userMapper.setFindIdByEmail(email);
                userMapper.setCreateUserRecord(id);
                content = "success";
            } else {
                content = "repeat";
            }
        } else {
            content = "codeError";
        }
        jsonObject.put("content", content);
        return jsonObject.toString();
    }

    @PostMapping("/user/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String passwd,
                        HttpSession session) {
        System.out.println("login: " + session.getId());
        System.out.println("login password:" + passwd);
        String passwdFromiot = userMapper.ContrastPasswd(email);
        String content = "";
        String token = TokenManager.createToken();
        session.setAttribute("token", token);
        if (passwd.equals(passwdFromiot)) {
            content = "success";
        } else {
            content = "fail";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserLogin");
        jsonObject.put("content", content);
        jsonObject.put("token", token);
        return jsonObject.toString();
    }

    @PostMapping("/user/changeEmail")
    public String changeEmail(@RequestParam("password") String password,
                              @RequestParam("newEmail") String newEmail,
                              @RequestParam("email") String email,
                              @RequestParam("token") String token,
                              @RequestParam("code") String code,
                              @RequestParam("type") String type, HttpSession session) {
        System.out.println("changeEmail: " + session.getId());
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken = "";
        String passwdFromiot = userMapper.ContrastPasswd(email);
        String content = "";
        //取出验证码
        Object o = session.getAttribute(type);
        String varCode = "";
        if (o == null) {
            System.out.println("session中没有值");
            varCode = "****";
        } else {
            varCode = (String) o;
        }
        //判断是否新邮箱已被注册
        User user = userMapper.setFindUserByEmail(newEmail);
        //判断token是否正确
        if (varToken.equals(token)) {
//        if (true){
            //判断password是否正确
            if (passwdFromiot.equals(password)) {
                //判断是否新邮箱已被注册
                if (user == null) {
                    //判断验证码是否正确
                    if (varCode.equalsIgnoreCase(code)) {
//                    if (true){
                        session.removeAttribute(type);
                        int id = userMapper.setFindIdByEmail(email);
                        userMapper.setUpdateEmailById(id, newEmail);
                        content = "success";
                        userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户修改了邮箱");
                    } else {
                        content = "codeError";
                    }
                } else {
                    content = "repeat";
                }
            } else {
                content = "passwordError";
            }
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserChangeEmail");
        jsonObject.put("token", varToken);
        jsonObject.put("content", content);
        return jsonObject.toString();
    }

    /**
     * 修改密码
     **/
    @PostMapping("/user/changePassword")
    public String changePassword(@RequestParam(name = "token") String token,
                                 @RequestParam(name = "email") String email,
                                 @RequestParam(name = "newPassword") String newPassword,
                                 @RequestParam(name = "code") String code,
                                 @RequestParam(name = "type") String type,
                                 HttpSession session) {
        System.out.println("changePassword: " + session.getId());
        String serverToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            serverToken = object.toString();
        }
        String content = "";
        if (!serverToken.equals(token))
//        if (false)
        {
            content = "tokenError";
        } else {
            Object o = session.getAttribute(type);
            String serverCode = "";
            if (o == null) {
                System.out.println("session中没有值");
                serverCode = "****";
            } else {
                serverCode = (String) o;
            }
            if (!code.equalsIgnoreCase(serverCode))
//            if (false)
            {
                content = "CodeError";
            } else {
                userMapper.setChangePasswordByEmail(newPassword, email);
                content = "success";
                int id = userMapper.setFindIdByEmail(email);
                userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户修改了密码");
            }
        }

        //产生新token并放入session
        String newToken = TokenManager.createToken();
        session.setAttribute("token", newToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserChangePassword");
        jsonObject.put("content", content);
        jsonObject.put("token", newToken);
        return jsonObject.toString();
    }

    @PostMapping("/user/addDevice")
    public String addDevice(@RequestParam("token") String token,
                            @RequestParam("serial") String serial,
                            @RequestParam("email") String email, HttpSession session) {
        System.out.println("addDevice: " + session.getId());
        JSONObject jsonObject = new JSONObject();
        String content = "";
        jsonObject.put("msgType", "UserAddDevice");
        int id = userMapper.setFindIdByEmail(email);
        List<String> devs = new ArrayList<>();
        String mydevs = userMapper.setFindDevByEmail(email);
        mydevs = mydevs.substring(1, mydevs.length() - 1);
        if (!mydevs.isEmpty()) {
            mydevs = mydevs.replace(" ", "");
            List<String> devTables = Arrays.asList(mydevs.split(","));
            for (int i = 0; i < devTables.size(); i++) {
                devs.add(devTables.get(i));
            }
        }
        //查找是否在未激活设备中
        InactiveDeviceModel inactiveDeviceModel = deviceMapper.setFindInDeviceByserial(serial);
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken;
        //判断token是否正确
        if (varToken.equals(token)) {
//        if (true) {
            //判断是否存在该未激活设备
            if (inactiveDeviceModel != null) {
                content = "success";
                ActiveDeviceModel model = new ActiveDeviceModel(inactiveDeviceModel.getMac(), inactiveDeviceModel.getSerial(), id);
                //创建数据表
                deviceMapper.setCreateTableByMac(inactiveDeviceModel.getMac());
                //插入激活设备表
                deviceMapper.setInsDeviceBySerial(model);
                //删除未激活设备记录
                deviceMapper.setDelDeviceBySerial(serial);
                //更新用户的设备列表
                devs.add(inactiveDeviceModel.getMac());
                userMapper.setAddDevByEmail(devs.toString(), email);
                userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户添加设备，序列号为: " + serial);
            } else {
                //若不存在该未激活设备，判断是否已被激活
                ActiveDeviceModel activeDeviceModel = deviceMapper.setFindActDeviceBySerial(serial);
                if (activeDeviceModel != null) {
                    content = "repeat";
                } else {
                    //都不是，则序列号不存在
                    content = "fail";
                }
            }
        } else {
            content = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    @PostMapping("/user/delDevice")
    public String delDevice(@RequestParam("token") String token,
                            @RequestParam("email") String email,
                            @RequestParam("mac") String mac, HttpSession session) {
        System.out.println("delDevice: " + session.getId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserDelDevice");
        String content = "";
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        User user = userMapper.setFindUserByEmail(email);
        String mydevs = user.getMydevices();
        List<String> devs = new ArrayList<>();
        mydevs = mydevs.substring(1, mydevs.length() - 1);
        if (!mydevs.isEmpty()) {
            mydevs = mydevs.replace(" ", "");
            List<String> devTables = Arrays.asList(mydevs.split(","));
            for (int i = 0; i < devTables.size(); i++) {
                devs.add(devTables.get(i));
            }
        }
        int i;
        int len = devs.size();
        for (i = 0; i < len; i++) {
            if (devs.get(i).equals(mac)) {
                devs.remove(mac);
                break;
            }
        }
        if (varToken.equals(token)) {
//        if (true) {
            if (i == len) {
                content = "fail";
            } else {
                //更新用户设备
                userMapper.setAddDevByEmail(devs.toString(), email);
                //删除活动表中数据
                deviceMapper.setDropTabByMac(mac);
                //添加到非活动表
                ActiveDeviceModel activeDeviceModel = deviceMapper.setFindActDeviceByMac(mac);
                deviceMapper.setInsInDevice(activeDeviceModel.getMac(), activeDeviceModel.getSerial());
                //删除活动表中数据数据表
                deviceMapper.setDelActDeviceByMac(mac);
                content = "success";
                deviceMapper.setDelMonitorByMac(mac);
                int id = userMapper.setFindIdByEmail(email);
                userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户删除了设备:" + mac);
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

    /**
     * 增加采集单元
     **/
    @PostMapping("/user/addUnit")
    public String addUnit(@RequestParam("token") String token,
                          @RequestParam("email") String email,
                          @RequestParam("name") String name,
                          @RequestParam("length") double length,
                          @RequestParam("width") double width,
                          @RequestParam("description") String description,
                          HttpSession session) {
        System.out.println("addUnit: " + session.getId());
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken = "";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserAddUnit");
        String content = "";
//        if (true){
        if (varToken.equals(token)) {
            //根据email找到id
            int user_id = userMapper.setFindIdByEmail(email);
            //遍历unit表，找到所有属于该用户的unit
            List<UnitModel> unitModels = userMapper.setFindUnitById(user_id);
            int i;
            //判断是否重复
            for (i = 0; i < unitModels.size(); i++) {
                if (unitModels.get(i).getName().equals(name)) {
                    content = "repeat";
                    break;
                }
            }
            if (i == unitModels.size()) {
                UnitModel model = new UnitModel(name, length, width, description, user_id);
                //插入unit记录
                userMapper.setInsUnit(model);
                content = "success";
                int id = userMapper.setFindIdByEmail(email);
                userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户添加采集单元: " + name);
            }
        } else {
            content = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }


    /**
     * 删除采集单元
     **/
    @PostMapping("/user/delUnit")
    public String delUnit(@RequestParam("token") String token,
                          @RequestParam("email") String email,
                          @RequestParam("name") String name, HttpSession session) {
        System.out.println("delUnit " + session.getId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserDelUnit");
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken = "";
        String content = "";
//        if(true){
        if (varToken.equals(token)) {
            //根据邮箱找到用户id
            int user_id = userMapper.setFindIdByEmail(email);
            //根据用户id和unit名称，删除绑定在该unit上的活动设备
            deviceMapper.setDelUnitInActDev(user_id, name);
            //删除unit表中的记录
            userMapper.setDelUnit(name, user_id);
            content = "success";
            int id = userMapper.setFindIdByEmail(email);
            userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户删除采集单元: " + name);
        } else {
            content = "tokenError";
        }

        //生成token
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("token", varToken);
        jsonObject.put("content", content);
        return jsonObject.toString();
    }

    @Value("${absoluteImgPath}")
    String absoluteImgPath;

    @Value("${sonImgPath}")
    String sonImgPath;

    @Value("${imageUrl}")
    String uploadUrl;

    @PostMapping("/user/changeAvatar")
    public String changeAvatar(@RequestParam("email") String email,
                               @RequestParam("token") String token,
                               @RequestParam("file") MultipartFile file, HttpSession session) {
        System.out.println("changeAvatar: " + session.getId());
        System.out.println("filename:" + file.getOriginalFilename());
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken = "";
        jsonObject.put("msgType", "UserChangeAvatar");
        String content = "";
//        if (true) {
        if (varToken.equals(token)) {
            if (file.isEmpty()) {
                content = "empty";
            } else {
                if (file.getSize() / 1048576.0 > 3) {
                    content = "big";
                } else {
                    String originalFilename = file.getOriginalFilename();
                    //随机生成文件名
                    String fileName = RandomUtils.createRandomString(10) + originalFilename;
                    File dest = new File(absoluteImgPath + fileName);
                    String imgUrl = uploadUrl + sonImgPath + fileName;
                    userMapper.setUpdateAvatar(imgUrl, email);
                    try {
                        //转存文件
                        file.transferTo(dest);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("保存的图片url: " + imgUrl);
                    content = "success";
                    int id = userMapper.setFindIdByEmail(email);
                    userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户修改了头像");
                }
            }
        } else {
            content = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    @PostMapping("/user/getUnitInfo")
    public String getUnitInfo(@RequestParam("email") String email,
                              @RequestParam("token") String token, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        System.out.println("getUnitInfo: " + session.getId());
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken;
        String msg = "";
//        if (true) {
        if (varToken.equals(token)) {
            // 查找用户id
            int user_id = userMapper.setFindIdByEmail(email);
            // 查找用户的unit
            List<UnitModel> unitModels = userMapper.setFindUnitById(user_id);
            if (!unitModels.isEmpty()) {
                for (int i = 0; i < unitModels.size(); i++) {
                    JSONObject var = new JSONObject();
                    var.put("unitName", unitModels.get(i).getName());
                    var.put("unitLength", unitModels.get(i).getLength());
                    var.put("unitWidth", unitModels.get(i).getWidth());
                    var.put("unitDescription", unitModels.get(i).getDescription());
                    jsonArray.put(var);
                }
                msg = "success";
            } else {
                msg = "noUnit";
            }
        } else {
            msg = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserGetUnitInfo");
        jsonObject.put("content", jsonArray.toString());
        jsonObject.put("token", varToken);
        jsonObject.put("message", msg);
        return jsonObject.toString();
    }

    @PostMapping("/user/changeUsername")
    public String changeUsername(@RequestParam("token") String token,
                                 @RequestParam("email") String email,
                                 @RequestParam("newUsername") String newUsername, HttpSession session) {
        System.out.println("changeUsername: " + session.getId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserChangeUsername");
        String content = "";
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken = "";
//        if (true) {
        if (varToken.equals(token)) {
            //根据email改变用户名
            userMapper.setUpdateUsername(newUsername, email);
            content = "success";
            int id = userMapper.setFindIdByEmail(email);
            userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户更改了用户名，新用户名为: " + newUsername);
        } else {
            content = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", token);
        jsonObject.put("token", varToken);
        jsonObject.put("content", content);
        return jsonObject.toString();
    }

    /**
     * 用户获取某个采集单元中设备的信息
     **/
    @PostMapping("/user/getDevsByUnit")
    public String getDevsByUnit(@RequestParam("email") String email,
                                @RequestParam("token") String token,
                                @RequestParam("unit") String unit, HttpSession session) {
        System.out.println("getDevsByUnit: " + session.getId());
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
//        String varToken;
        String msg = "";
//        if (true) {
        if (varToken.equals(token)) {
            //查找用户id
            int user_id = userMapper.setFindIdByEmail(email);
            //查找用户在某个unit中的所有设备
            List<ActiveDeviceModel> activeDeviceModels = deviceMapper.setFindDevsByUnit(user_id, unit);
            if (!activeDeviceModels.isEmpty()) {
                for (int i = 0; i < activeDeviceModels.size(); i++) {
                    JSONObject var = new JSONObject();
                    var.put("deviceMac", activeDeviceModels.get(i).getMac());
                    var.put("deviceSensor", activeDeviceModels.get(i).getSensor());
                    var.put("device", activeDeviceModels.get(i).getUnit());
                    var.put("deviceEquipment", activeDeviceModels.get(i).getEquipment());
                    jsonArray.put(var);
                }
                msg = "success";
                int id = userMapper.setFindIdByEmail(email);
                userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户查看了采集单元 " + unit + " 中所有的设备信息");
            } else {
                msg = "noDevice";
            }
        } else {
            msg = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserGetDevsByUnit");
        jsonObject.put("content", jsonArray.toString());
        jsonObject.put("token", varToken);
        jsonObject.put("message", msg);
        return jsonObject.toString();
    }

    /**
     * 用户删除采集单元中的设备
     **/
    @PostMapping("/user/delDevsInUnit")
    public String delDevsInUnit(@RequestParam("mac") String mac,
                                @RequestParam("token") String token,
                                @RequestParam("unit") String unit,
                                @RequestParam("email") String email,
                                HttpSession session) {
        System.out.println("delDevsInUnit: " + session.getId());
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        jsonObject.put("msgType", "UserDelDevsInUnit");
        String content = "";
        if (varToken.equals(token)) {
            //根据mac更新表即可，不用判断时候为该用户，mac不可能重复
            deviceMapper.setDelDevsInUnit(mac, unit);
            content = "success";
            int id = userMapper.setFindIdByEmail(email);
            userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "删除采集单元 " + unit + " 中的 " + mac + " 设备");
        } else {
            content = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    /**
     * 用户查看个人信息
     **/
    @PostMapping("/user/getUserInfo")
    public String getUserInfo(@RequestParam(name = "token") String token,
                              @RequestParam(name = "email") String email,
                              HttpSession session) {
        System.out.println("getUserInfo: " + session.getId());
        String varToken = "";
        String content = "";
        String message = "";
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            //从数据库中读出用户的个人信息
            if (token.equals(varToken)) {
                //
                User user = userMapper.setFindUserByEmail(email);
                JSONObject jsonObjectForContent = new JSONObject();
                jsonObjectForContent.put("userName", user.getName());
                jsonObjectForContent.put("userAvatar", user.getAvatar());
                jsonObjectForContent.put("userRegistertime", user.getRegister_time());
                jsonObjectForContent.put("userEmail", user.getEmail());
                content = jsonObjectForContent.toString();
                message = "success";
            } else {
                message = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserGetUserInfo");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        jsonObject.put("message", message);
        return jsonObject.toString();
    }

    /**
     * 全部设备信息
     **/
    @PostMapping("/user/getDeviceInfo")
    public String getDeviceInfo(@RequestParam(name = "token") String token,
                                @RequestParam(name = "email") String email,
                                HttpSession session) {
        String varToken = "";
        String content = "";
        String message = "";
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
                int userId = userMapper.setFindIdByEmail(email);
                List<ActiveDeviceModel> totalDevice = deviceMapper.setFindTotalDeviceById(userId);
                if (totalDevice.size() != 0) {
                    JSONArray jsonObjectsForContent = new JSONArray();
                    //遍历用户的设备，将设备信息存入json并放入json列表
                    int i;
                    for (i = 0; i < totalDevice.size(); i++) {
                        JSONObject jsonObjectForADevice = new JSONObject();
                        jsonObjectForADevice.put("deviceMac", totalDevice.get(i).getMac());
                        jsonObjectForADevice.put("deviceUnit", totalDevice.get(i).getUnit());
                        jsonObjectForADevice.put("deviceSensor", totalDevice.get(i).getSensor());
                        jsonObjectForADevice.put("deviceEquipment", totalDevice.get(i).getEquipment());
                        jsonObjectForADevice.put("deviceInterval", totalDevice.get(i).getInterval());
                        jsonObjectsForContent.put(jsonObjectForADevice);
                    }
                    content = jsonObjectsForContent.toString();
                    message = "success";
                } else {
                    message = "noDevice";
                }
            } else {
                message = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        varToken = TokenManager.createToken();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserGetDeviceInfo");
        session.setAttribute("token", varToken);
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        jsonObject.put("message", message);
        return jsonObject.toString();
    }

    /**
     * 获取全部传感器
     **/
    @PostMapping("/user/getSensorInfo")
    public String getSensorInfo(@RequestParam(name = "token") String token,
                                @RequestParam(name = "email") String email,
                                HttpSession session) {
        String varToken = "";
        String content = "";
        String message = "";
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
                int userId = userMapper.setFindIdByEmail(email);
                List<SensorModel> totalSensor = deviceMapper.setFindTotalSensorById(userId);

                if (totalSensor.size() != 0) {
                    JSONArray jsonObjectsForContent = new JSONArray();

                    //遍历用户的传感器，将传感器信息存入json并放入json列表
                    int i;
                    for (i = 0; i < totalSensor.size(); i++) {
                        JSONObject jsonObjectForASensor = new JSONObject();
                        jsonObjectForASensor.put("sensorName", totalSensor.get(i).getName());
                        jsonObjectForASensor.put("sensorOrder", totalSensor.get(i).getOrder());
                        jsonObjectForASensor.put("sensorMinisensor", totalSensor.get(i).getMini_sensor());
                        jsonObjectsForContent.put(jsonObjectForASensor);
                    }
                    content = jsonObjectsForContent.toString();
                    message = "success";
                } else {
                    message = "noSensor";
                }
            } else {
                message = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserGetSensorInfo");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        jsonObject.put("message", message);
        return jsonObject.toString();
    }

    @PostMapping("/user/resetUnit")
    public String resetUnit(@RequestParam("token") String token,
                            @RequestParam("name") String name,
                            @RequestParam("length") double length,
                            @RequestParam("width") double width,
                            @RequestParam("description") String description,
                            @RequestParam("email") String email, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        String content = "";
        int user_id = 0;
        if (varToken.equals(token)) {
            user_id = userMapper.setFindIdByEmail(email);
            UnitModel unitModel = new UnitModel(name, length, width, description, user_id);
            userMapper.setUpdateUnit(unitModel);
            content = "success";
            userMapper.setInsUserRecord(user_id, DetermineTime.getDateTime(), "用户更新了采集单元: " + name + " 的信息");
        } else {
            content = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserResetUnit");
        jsonObject.put("token", varToken);
        jsonObject.put("content", content);
        return jsonObject.toString();
    }

    @PostMapping("/user/getSensorInDevs")
    public String getSensorInDevs(@RequestParam("token") String token,
                                  @RequestParam("email") String email,
                                  @RequestParam("mac") String mac,
                                  HttpSession session) {
        String varToken = "";
        String content = "";
        String message = "";
        int userId = 0;
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
                userId = userMapper.setFindIdByEmail(email);
                List<SensorModel> list = deviceMapper.setFindTotalSensorById(userId);
                List<SensorModel> totalSensor = new ArrayList<>();
                String devSensor = deviceMapper.setFindSensorByMac(mac);
                devSensor = devSensor.substring(1, devSensor.length() - 1);
                if (!devSensor.isEmpty()) {
                    devSensor = devSensor.replace(" ", "");
                    List<String> sensorTables = Arrays.asList(devSensor.split(","));
                    for (int i = 0; i < sensorTables.size(); i++) {
                        for (int j = 0; j < list.size(); j++) {
                            if (sensorTables.get(i).equals(list.get(j).getName())) {
                                totalSensor.add(list.get(j));
                            }
                        }
                    }
                }
                if (totalSensor.size() != 0) {
                    JSONArray jsonObjectsForContent = new JSONArray();
                    //遍历用户的传感器，将传感器信息存入json并放入json列表
                    int i;
                    for (i = 0; i < totalSensor.size(); i++) {
                        JSONObject jsonObjectForASensor = new JSONObject();
                        jsonObjectForASensor.put("sensorName", totalSensor.get(i).getName());
                        jsonObjectForASensor.put("sensorOrder", totalSensor.get(i).getOrder());
                        jsonObjectForASensor.put("sensorMinisensor", totalSensor.get(i).getMini_sensor());
                        jsonObjectsForContent.put(jsonObjectForASensor);
                    }
                    content = jsonObjectsForContent.toString();
                    message = "success";
                } else {
                    message = "noSensor";
                }
            } else {
                message = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserGetSensorInDevs");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        jsonObject.put("message", message);
        return jsonObject.toString();
    }

    @PostMapping("/user/getEquipmentInDevs")
    public String getEquipmentInDevs(@RequestParam("token") String token,
                                     @RequestParam("mac") String mac,
                                     @RequestParam("email") String email, HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        Object object = session.getAttribute("token");
        if (object != null) {
            varToken = object.toString();
        }
        JSONArray jsonArray = new JSONArray();
        String message = "";
        if (varToken.equals(token)) {
            List<EquipmentModel> list = deviceMapper.setGetEquipmentByMac(mac);
            if (!list.isEmpty()) {
                message = "success";
                for (int i = 0; i < list.size(); i++) {
                    JSONObject var = new JSONObject();
                    var.put("equipmentName", list.get(i).getName());
                    var.put("equipmentRelay", list.get(i).getRelay());
                    var.put("equipmentStatus", list.get(i).isStatus());
                    var.put("equipmentOpenOrder", list.get(i).getOpen_order());
                    var.put("equipmentCloseOrder", list.get(i).getClose_order());
                    var.put("equipmentQueryOrder", list.get(i).getQuery_order());
                    jsonArray.put(var);
                }
            } else {
                message = "noEquipment";
            }
        } else {
            message = "tokenError";
        }

        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserGetEquipmentInDevs");
        jsonObject.put("content", jsonArray.toString());
        jsonObject.put("token", varToken);
        jsonObject.put("message", message);
        return jsonObject.toString();
    }


    /**
     * 用户修改已有强电设备
     **/
    @PostMapping("/user/resetEquipment")
    public String resetEquipment(@RequestParam(name = "token") String token,
                                 @RequestParam(name = "mac") String mac,
                                 @RequestParam(name = "email") String email,
                                 @RequestParam(name = "relay") int relay,
                                 @RequestParam(name = "open_order") String open_order,
                                 @RequestParam(name = "close_order") String close_order,
                                 @RequestParam(name = "query_order") String query_order,
                                 @RequestParam(name = "name") String equipmentName,
                                 HttpSession session) {
        String varToken = "";
        String content = "";
        int userId = 0;
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
                userId = userMapper.setFindIdByEmail(email);
                deviceMapper.setUpdateByMacandUserid(relay, open_order, close_order, query_order, userId, mac, equipmentName);
                content = "success";
                userMapper.setInsUserRecord(userId, DetermineTime.getDateTime(), "用户重设强电设备: " + equipmentName + " 信息");
            } else {
                content = "tokenError";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        varToken = TokenManager.createToken();
        JSONObject jsonObject = new JSONObject();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserResetDeviceEquipment");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    /**
     * 删除某设备上的某强电设备
     **/
    @PostMapping("/user/delEquipment")
    public String delEquipment(@RequestParam(name = "token") String token,
                               @RequestParam(name = "mac") String mac,
                               @RequestParam(name = "email") String email,
                               @RequestParam(name = "name") String equipmentName,
                               HttpSession session) {
        String varToken = "";
        String content = "";
        int userId = 0;
        try {
            Object object = session.getAttribute("token");
            if (object != null) {
                varToken = object.toString();
            }
            if (token.equals(varToken)) {
                userId = userMapper.setFindIdByEmail(email);
                //删除该强电设备记录
                deviceMapper.setDeleteAEquipmentByMacandUseridandName(userId, mac, equipmentName);
                //读出设备表中的强电设备列表，并删除该强电设备的名称
                String equipmentStr = deviceMapper.setFindEquipmentByMacandUserid(mac, userId);
                equipmentStr = equipmentStr.substring(1, equipmentStr.length() - 1);
                equipmentStr = equipmentStr.replace(" ", "");
                List<String> equipmentStrList = Arrays.asList(equipmentStr.split(","));
                List<String> equipmentList = new ArrayList<>();
                int equipmentListSize = equipmentStrList.size();
                for (int i = 0; i < equipmentListSize; i++) {
                    equipmentList.add(equipmentStrList.get(i));
                }
                if (equipmentListSize != 0) {
                    for (int i = 0; i < equipmentListSize; i++) {
                        if (equipmentList.get(i).equals(equipmentName)) {
                            equipmentList.remove(equipmentName);
                            break;
                        }
                    }
                    equipmentStr = equipmentList.toString();
                    deviceMapper.setUpdateEquipmentByMacandUserid(equipmentStr, mac, userId);
                    content = "success";
                    userMapper.setInsUserRecord(userId, DetermineTime.getDateTime(), "用户删除了传感器: " + equipmentName);
                } else {
                    content = "noEquipment";
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
        jsonObject.put("msgType", "UserDelDeviceEquipment");
        jsonObject.put("content", content);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    @PostMapping("/user/getRealTimeData")
    public String getRealTimeData(@RequestParam("mac") String mac,
                                  @RequestParam("token") String token,
                                  @RequestParam("email") String email,
                                  HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        String content = "";
        String message = "";
        Object o = session.getAttribute("token");
        if (o != null) {
            varToken = o.toString();
        }
        if (varToken.equals(token)) {
            Object oj = userMapper.setFindRealTimeData(mac);
            if (oj != null) {
                DataModel dataModel = userMapper.setFindRealTimeData(mac);
                JSONObject object = new JSONObject();
                object.put("record_time", dataModel.getRecord_time());
                object.put("data", dataModel.getData());
                content = object.toString();
                message = "success";
            } else {
                message = "noData";
            }
        } else {
            message = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserGetRealTimeData");
        jsonObject.put("content", content);
        jsonObject.put("message", message);
        jsonObject.put("token", varToken);

        return jsonObject.toString();
    }

    @PostMapping("/user/getHistoryData")
    public String getHistoryData(@RequestParam("mac") String mac,
                                 @RequestParam("token") String token,
                                 @RequestParam("start_time") String start_time,
                                 @RequestParam("end_time") String end_time,
                                 @RequestParam("email") String email,
                                 HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        String varToken = "";
        String content = "";
        String message = "";
        Object o = session.getAttribute("token");
        if (o != null) {
            varToken = o.toString();
        }
        if (varToken.equals(token)) {
            List<DataModel> dataModels = userMapper.setFindHistoryData(mac, start_time, end_time);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < dataModels.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("record_time", dataModels.get(i).getRecord_time());
                object.put("data", dataModels.get(i).getData());
                jsonArray.put(object);
            }
            content = jsonArray.toString();
            if (content.equals("[]")) {
                message = "noData";
            } else {
                int id = userMapper.setFindIdByEmail(email);
                userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户获取了在" + start_time + "和" + end_time + "之间的历史数据");
                message = "success";
            }
        } else {
            message = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserGetHistoryData");
        jsonObject.put("content", content);
        jsonObject.put("message", message);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    @PostMapping("/user/getLog")
    public String getLog(@RequestParam("email") String email,
                         @RequestParam("start_time") String start_time,
                         @RequestParam("end_time") String end_time,
                         @RequestParam("token") String token,
                         HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        int id = 0;
        String varToken = "";
        String content = "";
        String message = "";
        Object o = session.getAttribute("token");
        if (o != null) {
            varToken = o.toString();
        }
        if (varToken.equals(token)) {
            id = userMapper.setFindIdByEmail(email);
            List<LogModel> dataModels = userMapper.setGetLog(id, start_time, end_time);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < dataModels.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("record_time", dataModels.get(i).getRecord_time());
                object.put("data", dataModels.get(i).getBehavior());
                jsonArray.put(object);
            }
            content = jsonArray.toString();
            if (content.equals("[]")) {
                message = "noData";
            } else {
                message = "success";
            }
        } else {
            message = "tokenError";
        }
        varToken = TokenManager.createToken();
        session.setAttribute("token", varToken);
        jsonObject.put("msgType", "UserGetLog");
        jsonObject.put("content", content);
        jsonObject.put("message", message);
        jsonObject.put("token", varToken);
        return jsonObject.toString();
    }

    /**
     * 修改密码
     **/
    @PostMapping("/user/forgetPassword")
    public String forgetPassword(@RequestParam(name = "email") String email,
                                 @RequestParam(name = "newPassword") String newPassword,
                                 @RequestParam(name = "code") String code,
                                 @RequestParam(name = "type") String type,
                                 HttpSession session) {
        String content = "";
        Object o = session.getAttribute(type);
        String serverCode = "";
        if (o == null) {
            System.out.println("session中没有值");
            serverCode = "****";
        } else {
            serverCode = (String) o;
        }
        if (!code.equalsIgnoreCase(serverCode))
        {
            content = "CodeError";
        } else {
            userMapper.setChangePasswordByEmail(newPassword, email);
            content = "success";
            int id = userMapper.setFindIdByEmail(email);
            userMapper.setInsUserRecord(id, DetermineTime.getDateTime(), "用户修改了密码");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msgType", "UserForgetPassword");
        jsonObject.put("content", content);
        return jsonObject.toString();
    }
}
