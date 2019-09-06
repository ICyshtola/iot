#ifndef READ_GAS_H_
#define READ_GAS_H_
#include "global.h"

/* 初始化设备，返回设备文件描述符 */
int init_dev(void);

/**
* Function: read_data()
* Description: 读取传感器数据，向传感器写入命令，读取传感器返回的数据
* Input: fd 是RS485设备文件描述符，commd 是写入的命令，commd_len 是命令的长度，data 是保存设备返回的数据
* Return: 成功则返回读取到数据大小，失败返回 0
*/
int read_data(int fd, unsigned char *commd, int commd_len, unsigned char *data);

/**
* Function: write_only()
* Description: 控制继电器开或关，向传感器写入命令，不保存返回的数据
* Input: fd 是RS485设备文件描述符， commd 是写入的命令， commd_len 是命令的长度
* Output: 成功则返回读取到数据大小，失败返回 0
*/
int write_only(int fd, unsigned char *commd, int commd_len);

/**
* Function: read_all()
* Description: 读取传感器返回的全部信息
* Input: fd 是RS485设备文件描述符，commd 是写入的命令，commd_len 是命令的长度，data 是保存设备返回的数据
* Return: 成功则返回读取到信息大小，失败返回 0
*/
int read_all(int fd, unsigned char *commd, int commd_len, unsigned char *data);

#endif
