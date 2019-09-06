#ifndef GLOBAL_H_
#define GLOBAL_H_
#include <pthread.h>
#include "MQTTAsync.h"

#define COMMD_SIZE 8			//RS485命令的长度
#define MAXLEN 4096				//主题的最大长度
#define SENSOR_MAX_COUNT 256	//传感器最大数量
#define LOCAL_CONTROL_BASIC_SIZE 32		//接收到本地控制消息的最小长度

//定义开发板采取何种操作，发布何种消息
#define EMPTY 0x00		//不发布
#define SENSOR_DATA 0x01	//发布传感器数据
#define RELAY_STATUS 0x02	//发布继电器状态
#define RELAY_CONTROL 0x03	//控制继电器并发布继电器状态
#define CAL_ERROR 0x04		//反馈计算方式错误
#define LOCAL_CONTROL 0x05	//反馈本地控制结果
#define DEBUG 0x06			//用于调试

//定义本地控制状态
#define UNDEFINED 0		//用户未定义规则
#define DEFINED 1		//定义了规则
#define DATA_UNUSUAL -1		//数据不在规则之内

//用于保存本地控制需要的信息
typedef struct local_control_
{
	unsigned char topic_tail[MAXLEN];
	int flag;	//本地控制的状态
	int max_control_needed;		//是否需要执行超过最大值的控制指令
	int min_control_needed;		//是否需要执行超过最小值的控制指令
	double min;					//传感器数据最小值
	int count_min;				//如果小于最小值需要执行命令的个数
	unsigned char *cmmd_min;	//小于最小值需要执行的命令
	double max;					//传感器数据最大值
	int count_max;				//如果大于最大值需要执行命令的个数
	unsigned char *cmmd_max;	//大于最大值需要执行的命令
	int count_query;
	unsigned char *cmmd_query;	//查询继电器状态的命令
}local_control;

typedef struct globals_
{
	pthread_mutex_t lock;		
	MQTTAsync sub_client;
	int stop;		//程序结束的标志位

	int cycle;	//传感器上传周期
	int sensor_data_count[SENSOR_MAX_COUNT];  //各个传感器的数据的个数
	int all_data_count;		//所有传感器数据的总个数
	char *cal_str;		//计算方式
	int commd_type;
	local_control *local_ctrl;		//保存本地控制的信息

	unsigned char *commd;	//要循环执行的命令，例如读取传感器数据
	int commd_count;		//单次循环要执行的命令的个数
	char topic_tail[MAXLEN];	//接受主题的尾部，将拼接到发送主题

	unsigned char *commd_once;	//只执行一次的命令
	int commd_once_count;
	char topic_tail_once[MAXLEN];
}globals;

#endif
