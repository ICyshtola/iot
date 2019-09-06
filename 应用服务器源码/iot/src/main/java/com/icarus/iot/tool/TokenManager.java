package com.icarus.iot.tool;

import java.util.UUID;

public class TokenManager {
    public static String createToken() {
        UUID uuid = UUID.randomUUID();
        // 得到对象产生的ID
        String token = uuid.toString();
        // 转换为大写
        token = token.toUpperCase();
        // 替换 “-”变成空格
        token = token.replaceAll("-", "");
        return token;
    }
}
