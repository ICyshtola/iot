#ifndef SUB_H_
#define SUB_H_

#include "global.h"
#define SUB_ADDRESS     "tcp://192.168.100.32:1883"
#define SUB_CLIENTID    "F4:5E:AB:57:F0:FF_Sub"
#define SUB_TOPIC       "CONTROL/F4:5E:AB:57:F0:FF/#"
#define SUB_QOS         0
#define SUB_TIMEOUT     10000L

/* 启动MQTT并开始订阅 */
void sub_run(globals *arg);

/* 断开与MQTT的连接 */
void sub_disconnect(MQTTAsync client);
#endif
