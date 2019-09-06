#include <stdio.h>
#include "global.h"
#include <sys/types.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <string.h>
#include "sub.h"
#include "log.h"
#include "pub.h"

static globals global;
int i = 0;

void signal_handler(int sig)
{
	int ret;
	printf("\n\nIN %s\n", __func__);
	global.stop = 1;

	printf("please wait thread exit..............\n\n\n");
	//sleep(global.cycle + 1);
	ret = pub_exit();
	if (ret != 0)
	{
		printf("pub_exit error\n");
	}

	sub_disconnect(global.sub_client);
	MQTTAsync_destroy(&global.sub_client);
	write_log("%s\n", "is stopped");
	close_log();

	exit(0);
	return;
}

int main(void)
{
	//初始化全局变量
	global.stop = 0;
	//global.topic_tail = NULL;
	global.commd = NULL;
	global.commd_once = NULL;
	global.commd_type = EMPTY;
	global.cal_str = NULL;
	global.local_ctrl = NULL;
	global.cycle = 3;
	global.all_data_count = 0;
	memset(global.sensor_data_count, 0, sizeof(global.sensor_data_count));

	//初始化日志
	if (NULL == init_log())
	{
		printf("init_log error\n");
		exit(1);
	}
	write_log("%s\n", "is running");

	if (SIG_ERR == signal(SIGINT, signal_handler))
	{
		perror("SIGINT signal error");
		exit(1);
	}
	
	//初始化互斥锁
	if (0 != pthread_mutex_init(&global.lock, NULL))
	{
		perror("pthread_cond_init error");
		exit(1);
	}
	
	//开启发布线程
	pub_run(&global);

	//开始订阅
	sub_run(&global);
	
	while(!global.stop)
	{
		sleep(1);
	}
	return 0;
}
