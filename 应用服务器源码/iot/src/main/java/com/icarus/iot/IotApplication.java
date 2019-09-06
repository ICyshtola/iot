package com.icarus.iot;

import com.icarus.iot.tool.InitMQTTConnect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.icarus.iot.mapper")
public class IotApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(IotApplication.class, args);
            InitMQTTConnect.initMQTTConnect.newMQTT();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
