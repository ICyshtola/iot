package com.icarus.iot.mapper;

import com.icarus.iot.model.DataModel;
import com.icarus.iot.model.LogModel;
import com.icarus.iot.model.UnitModel;
import com.icarus.iot.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    String mysqlUserDb = "2019_User";

    String mysqlUnitDb = "unit";

    /**
     * 添加一条用户记录
     **/
    String insert_user = "insert into " + mysqlUserDb + " (name,password,avatar,email,register_time,last_modified_time) values (#{name},#{password},#{avatar},#{email},#{register_time},#{last_modified_time})";

    @Insert(insert_user)
    void setInsert_user(User user);

    /***通过email查找用户*/
    String findUserByEmail = "select * from " + mysqlUserDb + " where email = #{email}";

    @Select(findUserByEmail)
    User setFindUserByEmail(@Param("email") String email);

    /**
     * 验证登录密码
     **/
    String select_passwd = "select password from " + mysqlUserDb + " where email = #{email}";

    @Select(select_passwd)
    String ContrastPasswd(@Param("email") String passwdFromiot);

    /**
     * 通过email查找id
     **/
    String findIdByEmail = "select id from " + mysqlUserDb + " where email = #{email}";

    @Select(findIdByEmail)
    int setFindIdByEmail(@Param("email") String email);

    /**
     * 通过id更新email
     **/
    String updateEmailById = "update " + mysqlUserDb + " set email = #{email} where id = #{id}";

    @Select(updateEmailById)
    void setUpdateEmailById(@Param("id") int id, @Param("email") String email);

    /**
     * 修改密码
     **/
    String changePasswordByEmail = "update " + mysqlUserDb + " set password = #{newPassword} where email = #{email}";

    @Update(changePasswordByEmail)
    void setChangePasswordByEmail(@Param("newPassword") String newPassword, @Param("email") String email);

    /**
     * 根据email查找用户设备
     **/
    String findDevByEmail = "select mydevices from " + mysqlUserDb + " where email = #{email}";

    @Select(findDevByEmail)
    String setFindDevByEmail(@Param("email") String email);

    /**
     * 根据email为用户添加设备
     **/
    String addDevByEmail = "update " + mysqlUserDb + " set mydevices = #{mydevices} where email = #{email}";

    @Select(addDevByEmail)
    String setAddDevByEmail(@Param("mydevices") String mydevices, @Param("email") String email);

    /**
     * 通过user_id读取该用户的所有采集单元信息
     **/
    String findUnitById = "select * from " + mysqlUnitDb + " where user_id = #{user_id}";

    @Select(findUnitById)
    List<UnitModel> setFindUnitById(@Param("user_id") int uer_id);

    /**
     * 插入unit记录
     **/
    String insUnit = "insert into " + mysqlUnitDb + " (`name`,`length`,`width`,`description`,`user_id`) values (#{name},#{length},#{width},#{description},#{user_id})";

    @Insert(insUnit)
    void setInsUnit(UnitModel unitModel);

    /**
     * 删除unit记录
     **/
    String delUnit = "delete from " + mysqlUnitDb + " where name = #{name} and user_id = #{user_id}";

    @Delete(delUnit)
    void setDelUnit(@Param("name") String name, @Param("user_id") int user_id);

    /**
     * 更新头像
     **/
    String updateAvatar = "update " + mysqlUserDb + " set avatar = #{avatar} where email = #{email}";

    @Update(updateAvatar)
    void setUpdateAvatar(@Param("avatar") String avatar, @Param("email") String email);

    /**
     * 更新用户名
     **/
    String updateUsername = "update " + mysqlUserDb + " set name = #{name} where email = #{email}";

    @Update(updateUsername)
    void setUpdateUsername(@Param("name") String name, @Param("email") String email);

    /**
     * 更新unit信息
     **/
    String updateUnit = "UPDATE `" + mysqlUnitDb + "` SET `length` = #{length}, `width` =  #{width}, `description` = #{description} WHERE `user_id` = #{user_id} and `name` = #{name}";

    @Update(updateUnit)
    void setUpdateUnit(UnitModel unitModel);

    /**
     * 获取实时数据
     **/
    String findRealTimeData = " SELECT * FROM `${mac}` ORDER BY  `record_time` DESC  LIMIT 1";

    @Select(findRealTimeData)
    DataModel setFindRealTimeData(@Param("mac") String mac);

    /**
     * 获取历史数据
     **/
    String findHistoryData = "select * from `${mac}` WHERE `record_time` BETWEEN '${start_time}' AND '${end_time}'";

    @Select(findHistoryData)
    List<DataModel> setFindHistoryData(@Param("mac") String mac, @Param("start_time") String start_time, @Param("end_time") String end_time);

    /**
     * 创建用户记录表
     **/
    String createUserRecord = "CREATE TABLE `record_${id}` ( `record_time` datetime NOT NULL, `behavior` varchar(512) NOT NULL, `id` int(20) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`) USING BTREE) ENGINE=InnoDB DEFAULT CHARSET=utf8";

    @Update(createUserRecord)
    void setCreateUserRecord(@Param("id") int id);

    /**
     * 插入用户记录
     **/
    String insUserRecord = "insert into `record_${id}` (`record_time`,`behavior`) values (#{date},#{behavior})";

    @Insert(insUserRecord)
    void setInsUserRecord(@Param("id") int id, @Param("date") String date, @Param("behavior") String behavior);

    /**
     * 用户获取操作日志
     **/
    String getLog = "select * from `record_${id}` where `record_time` between '${start_time}' and '${end_time}'";

    @Select(getLog)
    List<LogModel> setGetLog(@Param("id") int id, @Param("start_time") String start_time, @Param("end_time") String end_time);
}
