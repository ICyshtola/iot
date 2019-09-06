package com.icarus.iot.tool;

import com.icarus.iot.mqttManager.ServerMQTT;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SeverMqttStart {
    /**
     * 启动函数
     **/
    public static void start(ServerMQTT serverMQTT, byte[] message, String topic) throws MqttException {
        //char MESSAGE[] = {0x02, 0x03, 0x00, 0x2A, 0x00, 0x01, 0xA5, 0xF1};

        serverMQTT.message = new MqttMessage();
        serverMQTT.message.setQos(1);
        serverMQTT.message.setRetained(false);
        serverMQTT.message.setPayload(message);
        serverMQTT.setTopic(topic);
        serverMQTT.publish(serverMQTT.mqttTopic, serverMQTT.message);
        System.out.println(serverMQTT.message.isRetained() + ": ratained状态");
    }

    public static byte[] charToByte(char ch[], int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (ch[i] & 0xFF);
        }
        return bytes;
    }

    public static byte aCharToByte(char ch) {
        byte bytes = (byte) (ch & 0xFF);
        return bytes;
    }

    public static byte[] strToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static float byteToFloat(byte[] b, int index) {
        int l;

        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);

        return Float.intBitsToFloat(l);
    }

    public static double byteToDouble(byte[] b, int index) {
        long l = 0;

        for (int i = 0; i < 8; i++) {
            l |= ((long) (b[index + i] & 0xff)) << (8 * i);
        }

        return Double.longBitsToDouble(l);
    }

    public static byte[] intToByte(int val) {
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        b[2] = (byte) ((val >> 16) & 0xff);
        b[3] = (byte) ((val >> 24) & 0xff);

        return b;
    }

    public static byte[] doubleToByte(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ( ( value >> 8 * i ) & 0xff );
        }
        return byteRet;
    }
}
