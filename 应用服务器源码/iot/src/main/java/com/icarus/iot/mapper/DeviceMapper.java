package com.icarus.iot.mapper;

import com.icarus.iot.model.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DeviceMapper {
    //未激活设备表
    String mysqlInDeviceDb = "inactive_devices";
    //激活表
    String mysqlDeviceDb = "active_devices";
    //传感器表
    String mysqlSensorDb = "sensor";
    //强电设备表
    String mysqlEquipmentDb = "equipment";
    //监控表
    String mysqlMonitorDb = "monitor";


    /**
     * 通过序列号找到激活的设备
     **/
    String findActDeviceBySerial = "select * from " + mysqlDeviceDb + " where serial = #{serial}";

    @Select(findActDeviceBySerial)
    ActiveDeviceModel setFindActDeviceBySerial(@Param("serial") String serial);

    /**
     * 通过mac找到激活的设备
     **/
    String findActDeviceByMac = "select * from " + mysqlDeviceDb + " where mac = #{mac}";

    @Select(findActDeviceByMac)
    ActiveDeviceModel setFindActDeviceByMac(@Param("mac") String mac);

    /**
     * 根据mac地址创建数据表
     **/
    String createTableByMac = "CREATE TABLE `${table}` (`record_time` datetime NOT NULL,`data` varchar(1024) NOT NULL,PRIMARY KEY (`record_time`)) ENGINE=InnoDB DEFAULT CHARSET=utf8";

    @Update(createTableByMac)
    void setCreateTableByMac(@Param("table") String table);

    /**
     * 根据序列号在未激活设备表中查找未激活设备设备
     **/
    String findInDeviceBySerial = "select * from " + mysqlInDeviceDb + " where serial = #{serial}";

    @Select(findInDeviceBySerial)
    InactiveDeviceModel setFindInDeviceByserial(@Param("serial") String serial);

    /**
     * 通过serial删除未激活表
     **/
    String delDeviceBySerial = "delete from " + mysqlInDeviceDb + " where serial = #{serial}";

    @Delete(delDeviceBySerial)
    void setDelDeviceBySerial(@Param("serial") String serial);

    /**
     * 插入激活设备
     **/
    String insDeviceBySerial = "insert into " + mysqlDeviceDb + " (mac,serial,user_id) values (#{mac},#{serial},#{user_id})";

    @Insert(insDeviceBySerial)
    void setInsDeviceBySerial(ActiveDeviceModel activeDeviceModel);

    /**
     * 插入未激活设备
     **/
    String insInDevice = "insert into " + mysqlInDeviceDb + " (mac,serial) values (#{mac},#{serial})";

    @Insert(insInDevice)
    void setInsInDevice(@Param("mac") String mac, @Param("serial") String serial);

    /**
     * 通过mac删除活动设备
     **/
    String delActDeviceByMac = "delete from " + mysqlDeviceDb + " where mac = #{mac}";

    @Delete(delActDeviceByMac)
    void setDelActDeviceByMac(@Param("mac") String mac);

    /**
     * 通过mac删除表
     **/
    String dropTabByMac = "drop table `${table}`";

    @Update(dropTabByMac)
    void setDropTabByMac(@Param("table") String table);

    /**
     * 通过用户id查找已有传感器
     **/
    String findSensorById = "select * from " + mysqlSensorDb + " where user_id = #{id}";

    @Select(findSensorById)
    List<SensorModel> setFindSensorById(@Param("id") int id);

    /**
     * 为传感器表插入记录
     **/
    String insSensor = "insert into `" + mysqlSensorDb + "` (`name`,`order`,`calculation`,`mini_sensor`,`user_id`) values (#{name},#{order},#{calculation},#{mini_sensor},#{user_id})";

    @Insert(insSensor)
    void setInsSensor(SensorModel sensor);

    /**
     * 更新传感器信息
     **/
    String updateSensor = "UPDATE `" + mysqlSensorDb + "` SET `order` = #{order}, `calculation` =  #{calculation}, `mini_sensor` = #{mini_sensor} WHERE `user_id` = #{user_id} and `name` = #{name}";

    @Update(updateSensor)
    void setUpdateSensor(SensorModel sensor);

    /**
     * 删除传感器
     **/
    String delSensorBySensorid = "delete from " + mysqlSensorDb + " where sensor_id = #{sensor_id}";

    @Delete(delSensorBySensorid)
    void setDelSensorBySensorid(@Param("sensor_id") int sensor_id);

    /**
     * 通过用户id和传感器名查找传感器id
     **/
    String findSensoridById = "select sensor_id from " + mysqlSensorDb + " where user_id = #{user_id} and name = #{name}";

    @Select(findSensoridById)
    int setFindSensoridById(@Param("user_id") int user_id, @Param("name") String name);

    /**
     * 通过用户id找到他的所有设备的mac
     **/
    String findMaclistByUserid = "select mac from " + mysqlDeviceDb + " where user_id = #{user_id}";

    @Select(findMaclistByUserid)
    List<String> setFindMaclistByUserid(@Param("user_id") int user_id);

    /**
     * 通过用户的设备mac找到这个设备拥有的传感器
     **/
    String findSensorByMac = "select sensor from " + mysqlDeviceDb + " where mac = #{mac}";

    @Select(findSensorByMac)
    String setFindSensorByMac(@Param("mac") String mac);

    /**
     * 更新用户设备的传感器字段
     **/
    String updateSensorByMac = "update " + mysqlDeviceDb + " set sensor = #{sensor} where mac = #{mac}";

    @Update(updateSensorByMac)
    void setUpdateSensorByMac(@Param("sensor") String sensor, @Param("mac") String mac);

    /**
     * 根据unit名称删除活动设备的绑定
     **/
    String delUnitInActDev = "update " + mysqlDeviceDb + " set unit = '' where user_id = #{user_id} and unit = #{unit}";

    @Update(delUnitInActDev)
    void setDelUnitInActDev(@Param("user_id") int user_id, @Param("unit") String unit);

    /**
     * 根据用户id和unit获取设备信息
     **/
    String findDevsByUnit = "select * from " + mysqlDeviceDb + " where user_id = #{user_id} and unit = #{unit}";

    @Select(findDevsByUnit)
    List<ActiveDeviceModel> setFindDevsByUnit(@Param("user_id") int user_id, @Param("unit") String unit);

    /**
     * 根据unit名称删除活动设备的绑定
     **/
    String delDevsInUnit = "update " + mysqlDeviceDb + " set unit = '' where mac = #{mac} and unit = #{unit}";

    @Update(delDevsInUnit)
    void setDelDevsInUnit(@Param("mac") String mac, @Param("unit") String unit);

    /**
     * 更新设备的采集单元信息
     **/
    String resetDeviceUnitandInterval = "update " + mysqlDeviceDb + " set unit = #{unit} ,`interval` = #{interval} where mac = #{mac}";

    @Update(resetDeviceUnitandInterval)
    void setResetDeviceUnitandInterval(@Param("unit") String unit, @Param("interval") int interval, @Param("mac") String mac);

    /**
     * 更新设备传感器
     **/
    String resetDeviceSensor = "update " + mysqlDeviceDb + " set sensor = #{sensor} where mac = #{mac}";

    @Update(resetDeviceSensor)
    void setResetDeviceSensor(@Param("sensor") String sensor, @Param("mac") String mac);

    /**
     * 获取设备表中的强电设备信息
     **/
    String getEquipmentInDevs = "select equipment from " + mysqlDeviceDb + " where mac = #{mac}";

    @Select(getEquipmentInDevs)
    String setGetEquipmentInDevs(@Param("mac") String mac);

    /**
     * 更新设备表中的强电设备信息
     **/
    String updateEquipmentInDevs = "update " + mysqlDeviceDb + " set equipment = #{equipment} where mac = #{mac}";

    @Update(updateEquipmentInDevs)
    void setUpdateEquipmentInDevs(@Param("equipment") String equipment, @Param("mac") String mac);

    /**
     * 插入强电设备表
     **/
    String insEquipment = "insert into " + mysqlEquipmentDb + " (`name`,`status`,`relay`,`mac`,`user_id`,`open_order`,`close_order`,`query_order`) values (#{name},#{status},#{relay},#{mac},#{user_id},#{open_order},#{close_order},#{query_order})";

    @Insert(insEquipment)
    void setInsEquipment(EquipmentModel equipment);

    /**
     * 根据mac获取某个设备的所有的强电设备
     **/
    String getEquipmentByMac = "select * from " + mysqlEquipmentDb + " where mac = #{mac}";

    @Select(getEquipmentByMac)
    List<EquipmentModel> setGetEquipmentByMac(@Param("mac") String mac);

    /**
     * 通过用户ID和设备mac和强电设备名更新该强电设备的继电器线路
     **/
    String updateByMacandUseridandName = "update " + mysqlEquipmentDb + " set relay = #{relay},open_order = #{open_order},close_order = #{close_order},query_order = #{query_order} where user_id = #{userId} and mac = #{mac} and name = #{name}";

    @Update(updateByMacandUseridandName)
    void setUpdateByMacandUserid(@Param("relay") int relay, @Param("open_order") String open_order, @Param("close_order") String close_order, @Param("query_order") String query_order, @Param("userId") int userId, @Param("mac") String mac, @Param("name") String name);

    /**
     * 通过用户ID和设备mac和强电设备名删除该强电设备的记录
     **/
    String deleteAEquipmentByMacandUseridandName = "delete from " + mysqlEquipmentDb + " where user_id = #{userId} and mac = #{mac} and name = #{name}";

    @Delete(deleteAEquipmentByMacandUseridandName)
    void setDeleteAEquipmentByMacandUseridandName(@Param("userId") int userId, @Param("mac") String mac, @Param("name") String name);

    /**
     * 通过设备mac和用户id取出设备表中的强电设备字段
     **/
    String findEquipmentByMacandUserid = "select equipment from " + mysqlDeviceDb + " where mac = #{mac} and user_id = #{userId}";

    @Select(findEquipmentByMacandUserid)
    String setFindEquipmentByMacandUserid(@Param("mac") String mac, @Param("userId") int userId);

    /**
     * 通过设备mac和用户id更新设备表中的强电设备字段
     **/
    String updateEquipmentByMacandUserid = "update " + mysqlDeviceDb + " set equipment = #{equipment} where mac = #{mac} and user_id = #{userId}";

    @Update(updateEquipmentByMacandUserid)
    void setUpdateEquipmentByMacandUserid(@Param("equipment") String equipment, @Param("mac") String mac, @Param("userId") int userId);

    /**
     * 查找用户的所有设备信息
     **/
    String findTotalDeviceById = "select * from " + mysqlDeviceDb + " where user_id = #{user_id}";

    @Select(findTotalDeviceById)
    List<ActiveDeviceModel> setFindTotalDeviceById(@Param("user_id") int user_id);

    /**
     * 查找用户的所有传感器信息
     **/
    String findTotalSensorById = "select * from " + mysqlSensorDb + " where user_id = #{user_id}";

    @Select(findTotalSensorById)
    List<SensorModel> setFindTotalSensorById(@Param("user_id") int user_id);

    /**
     * 通过用户ID和MAC找这个设备的传感器
     **/
    String findSensorByIdandMac = "select sensor from " + mysqlDeviceDb + " where user_id = #{user_id} and mac = #{mac}";

    @Select(findSensorByIdandMac)
    String setFindSensorByIdandMac(@Param("user_id") int user_id, @Param("mac") String mac);

    /**
     * 通过传感器名字和用户id查找这个传感器的记录
     **/
    String findSensorByNameandUserid = "select * from " + mysqlSensorDb + " where name=#{sensorName} and user_id = #{userId}";

    @Select(findSensorByNameandUserid)
    SensorModel setFindSensorByNameandUserid(@Param("sensorName") String sensorName, @Param("userId") int userId);

    /**
     * 通过mac获得发送间隔
     **/
    String findIntervalByMac = "select `interval` from " + mysqlDeviceDb + " where `mac` = #{mac}";

    @Select(findIntervalByMac)
    int setFindIntervalByMac(@Param("mac") String mac);

    /**
     * 向设备数据表中插入一条数据
     **/
    String insertDataInMac = "insert into `${Mac}` (`data` , `record_time`) values(#{data}, #{recordTime})";

    @Insert(insertDataInMac)
    void setInsertDataInMac(@Param("Mac") String Mac, @Param("data") String data, @Param("recordTime") String recordTime);

    /**
     * 更新强电设备状态
     **/
    String updateStatusInEquipment = "update " + mysqlEquipmentDb + " set status = #{status} where name = #{name} and mac = #{mac} and user_id = #{userId}";

    @Update(updateStatusInEquipment)
    void setUpdateStatusInEquipment(@Param("status") boolean status, @Param("name") String name, @Param("mac") String mac, @Param("userId") int userId);

    /**
     * 查找某强电设备的信息
     **/
    String findEquipmentInequipment = "select * from " + mysqlEquipmentDb + " where name = #{name} and mac = #{mac} and user_id = #{userId}";

    @Select(findEquipmentInequipment)
    EquipmentModel setFindEquipmentInequipment(@Param("name") String name, @Param("mac") String mac, @Param("userId") int userId);

    /**
     * 通过sensor_name和mac和mini_sensor_name查找mini_sensor_name，以此判断这条记录是否存在
     **/
    String findMinisensornameInMonitor = "select mini_sensor_name from " + mysqlMonitorDb + " where sensor_name = #{sensorName} and mini_sensor_name = #{minisensorName} and mac = #{mac}";

    @Select(findMinisensornameInMonitor)
    String setFindMinisensornameInMonitor(@Param("sensorName") String sensorName, @Param("minisensorName") String minisensorName, @Param("mac") String mac);

    /**
     * 向控制表插入一条数据
     **/
    String insertMonitor = "insert into " + mysqlMonitorDb + " (mini_sensor_name,sensor_name,max,min,min_order,max_order,equipment_name,mac) values (#{mini_sensor_name},#{sensor_name},#{max},#{min},#{min_order},#{max_order},#{equipment_name},#{mac})";

    @Insert(insertMonitor)
    void setInsertMonitor(MonitorModel monitorModel);

    /**
     * 更新控制表的一条数据
     **/
    String updateMonitor = "update " + mysqlMonitorDb + " set max = #{max}, min = #{min}, min_order = #{min_order}, max_order = #{max_order}, equipment_name = #{equipment_name} where sensor_name = #{sensor_name} and mini_sensor_name = #{mini_sensor_name} and mac = #{mac}";

    @Update(updateMonitor)
    void setUpdateMonitor(MonitorModel monitorModel);

    /**
     * 查找某设备的某大传感器在监控表中的小传感器list
     **/
    String findAllMinisensornameInMonitorByMacandSensorname = "select mini_sensor_name from " + mysqlMonitorDb + " where mac = #{mac} and sensor_name = #{sensorName}";

    @Select(findAllMinisensornameInMonitorByMacandSensorname)
    List<String> setFindAllMinisensornameInMonitorByMacandSensorname(@Param("mac") String mac, @Param("sensorName") String sensorName);

    /**
     * 通过mac和sensor_name和mini_sensor_name查找整条记录
     **/
    String findMinisensorInMonitor = "select * from " + mysqlMonitorDb + " where mac = #{mac} and sensor_name = #{sensorName} and mini_sensor_name = #{minisensorName}";

    @Select(findMinisensorInMonitor)
    MonitorModel setFindMinisensorInMonitor(@Param("mac") String mac, @Param("sensorName") String sensorName, @Param("minisensorName") String minisensorName);

    /**
     * 通过mac和equipmentName查找它的查询命令
     **/
    String findQueryorderByMacandEquipmentname = "select query_order from " + mysqlEquipmentDb + " where mac = #{mac} and name = #{equipmentName}";

    @Select(findQueryorderByMacandEquipmentname)
    String setFindQueryorderByMacandEquipmentname(@Param("mac") String mac, @Param("equipmentName") String equipmentName);

    /**
     * 通过强电设备名和mac更新强电设备状态
     **/
    String updateStatusByMacandEquipmentname = "update " + mysqlEquipmentDb + " set status = #{status} where name = #{equipmentName} and mac = #{mac}";

    @Select(updateStatusByMacandEquipmentname)
    void setUpdateStatusByMacandEquipmentname(@Param("status") int status, @Param("equipmentName") String equipmentName, @Param("mac") String mac);

    /**删除关联传感器的本地监控表**/
    String delMonitorByMac = "delete  from " + mysqlMonitorDb + " where `mac` = #{mac}";

    @Delete(delMonitorByMac)
    void setDelMonitorByMac(@Param("mac") String mac);

}
