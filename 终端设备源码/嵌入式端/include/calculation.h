#ifndef CALCULAtION_H
#define CALCULAtION_H

/**
* Function: mycal()
* Input: cal_str 是用于计算的表达式（包含多个数据的表达式），data 存放原始数据，real_data 存放计算好的数据，data_size 是 data 中需要计算的长度
* return: 成功返回被截取的计算表达式 cal_str（用过的计算表达式将被截取掉）,失败返回 NULL
*/
char *mycal(char *cal_str, char data[], double real_data[], int data_size, int data_count);

#endif
