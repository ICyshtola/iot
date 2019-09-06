#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include <float.h>
#include "MQTTAsync.h"
#include "read_data.h"
#include "calculation.h"
#include "log.h"
#include "pub.h"

static globals *pglobal;
int fd;
pthread_t pub_thread_id, pub_data_thread_id;

void pub_onSend(void* context, MQTTAsync_successData* response)
{
	printf("token:%d, send successfully\n\n", response->token);
}

void *pub_data_thread(void *arg)
{
	MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
	MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
	int rc, i, j, z, data_len, topic_len, data_count = 0, ret;
	unsigned char *all_data = NULL;
	unsigned char data[MAXLEN];
	char pub_topic[MAXLEN], *cal_start = NULL, *cal_end = NULL;
	struct timeval start, end;
	long int time_use;
	double real_data[MAXLEN / 2];

	opts.onSuccess = pub_onSend;
	opts.context = pglobal->sub_client;
	pubmsg.qos = PUB_QOS;
	pubmsg.retained = 0;
	
	while (!pglobal->stop)
	{
		gettimeofday(&start, NULL);
		pthread_mutex_lock(&pglobal->lock);
		if (pglobal->commd_type == SENSOR_DATA)
		{
			if (pglobal->cal_str == NULL)  //如果没有得到计算方式，反馈给服务器
			{
				pglobal->commd_type = CAL_ERROR;
				break;
			}

			sprintf(pub_topic, "%s%s", PUB_TOPIC_HEAD, pglobal->topic_tail); //拼接发送主题
			memset(data, 0, MAXLEN);
			data_count = 0;
			cal_start = pglobal->cal_str;
			for (i = 0; i < pglobal->commd_count; i++)
			{
				data_len = read_data(fd, pglobal->commd + i*COMMD_SIZE, COMMD_SIZE, data);

				for (j = 0; j < data_len; j++)
				{
					printf("%02x ", data[j]);
				}
				printf("\n");

				if (pglobal->sensor_data_count[i] > data_len / 2)
				{
					//printf("data_len error\n");
					write_log("%d data_len error\n", i);
					for (j = 0; j < pglobal->sensor_data_count[i]; j++)
					{
						real_data[data_count + j] = DBL_MAX;
						cal_start = strchr(cal_start, '_');
						if (cal_start == NULL)
						{
							pglobal->commd_type = CAL_ERROR;
							break;
						}
						cal_start++;
					}
					data_count += pglobal->sensor_data_count[i];
					continue;
				}

				cal_start = mycal(cal_start, data, real_data + data_count, data_len, pglobal->sensor_data_count[i]);
				if (cal_start == NULL)
				{
					pglobal->commd_type = CAL_ERROR;
					break;
				}

				//在此处进行本地控制
				for (j = data_count; j < data_count + data_len / 2; j++)
				{
					//如果用户没有定义规则，跳过
					if (pglobal->local_ctrl[j].flag == UNDEFINED)
					{
						continue;
					}

					//根据用户定义的规则，判断数据是否异常
					if (real_data[j] < pglobal->local_ctrl[j].min && pglobal->local_ctrl[j].min_control_needed == 1)
					{
						//printf("min\n");
						write_log("Data smaller than minimum\n");
						pglobal->local_ctrl[j].min_control_needed = 0;
						pglobal->local_ctrl[j].max_control_needed = 1;
						for (z = 0; z < pglobal->local_ctrl[j].count_min; z++)
						{
							ret = write_only(fd, pglobal->local_ctrl[j].cmmd_min + z * COMMD_SIZE, COMMD_SIZE);	//控制继电器
							if (ret < 0)
							{
								//printf("local control failed\n");
								write_log("local control failed\n");
								pglobal->local_ctrl[j].min_control_needed = 1;
							}
						}
					    pglobal->commd_type = LOCAL_CONTROL;
						pglobal->local_ctrl[j].flag = DATA_UNUSUAL;
					}
					else if (real_data[j] > pglobal->local_ctrl[j].max && pglobal->local_ctrl[j].max_control_needed == 1)
					{
						//printf("max\n");
						write_log("Data larger than maximum\n");
						pglobal->local_ctrl[j].max_control_needed = 0;
						pglobal->local_ctrl[j].min_control_needed = 1;
						for (z = 0; z < pglobal->local_ctrl[j].count_max; z++)
						{
							ret = write_only(fd, pglobal->local_ctrl[j].cmmd_max + z * COMMD_SIZE, COMMD_SIZE);	//控制继电器
							if (ret < 0)
							{
								//printf("local control failed\n");
								write_log("local control failed\n");
								pglobal->local_ctrl[j].max_control_needed = 1;
							}
						}
					    pglobal->commd_type = LOCAL_CONTROL;
						pglobal->local_ctrl[j].flag = DATA_UNUSUAL;
					}
					/*else	
					{
						printf("in else\n");
						pglobal->local_ctrl[j].min_control_needed = 1;
						pglobal->local_ctrl[j].max_control_needed = 1;
					}*/
				}
					//

				data_count += data_len / 2;
			}
			pubmsg.payload = real_data;
			pubmsg.payloadlen = data_count * sizeof(double);
		}
		else
		{
			pthread_mutex_unlock(&pglobal->lock);
			usleep(10000);
			continue;
		}
		
		pthread_mutex_unlock(&pglobal->lock);
		
		gettimeofday(&end, NULL);
		time_use = 1000000 * (end.tv_sec - start.tv_sec) + end.tv_usec - start.tv_usec;
		//printf("cycle = %d\n", pglobal->cycle);
		if (time_use < 1000000 * pglobal->cycle)
		{
			usleep(1000000 * pglobal->cycle - time_use);
		}

		if ((rc = MQTTAsync_sendMessage(pglobal->sub_client, pub_topic, &pubmsg, &opts)) != MQTTASYNC_SUCCESS)
		{
			printf("Failed to start sendMessage, return code %d\n", rc);
		}
	}
	return NULL;
}

void *pub_thread(void *arg)
{
	MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
	MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
	int rc, i, j, z, data_len, topic_len, data_count = 0, message_len = 0;
	unsigned char *all_data = NULL, *p_str = NULL;
	unsigned char data[MAXLEN], temp[2 * MAXLEN];
	char pub_topic[MAXLEN], *cal_start = NULL, *cal_end = NULL;
	struct timeval start, end;
	long int time_use;
	double real_data[MAXLEN / 2];
	unsigned char key[] = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};

	opts.onSuccess = pub_onSend;
	opts.context = pglobal->sub_client;
	pubmsg.qos = PUB_QOS;
	pubmsg.retained = 0;
	
	while (!pglobal->stop)
	{
		pthread_mutex_lock(&pglobal->lock);

		//根据命令类型回复
		switch(pglobal->commd_type)
		{
			case SENSOR_DATA:
			case EMPTY:
				pthread_mutex_unlock(&pglobal->lock);
				usleep(100000);
				continue;
				break;
			case DEBUG:
				memset(data, 0 ,sizeof(data));
				memset(temp, 0, sizeof(temp));
				sprintf(pub_topic, "%s%s", PUB_TOPIC_HEAD, pglobal->topic_tail_once); //拼接发送主题
				data_len = read_all(fd, pglobal->commd_once, COMMD_SIZE, data);
				for (i = 0; i < data_len - 1; i++)
				{
					sprintf(temp, "%s%02X ", temp, data[i]);
				}
				printf("temp = %s\n", temp);
				pubmsg.payload = temp;
				pubmsg.payloadlen = strlen(temp);
				pglobal->commd_type = (pglobal->commd == NULL) ? EMPTY : SENSOR_DATA;
				break;
			case CAL_ERROR:
				pubmsg.payload = NULL;
				pubmsg.payloadlen = 0;
				sprintf(pub_topic, "%sCAL_ERROR", PUB_TOPIC_HEAD);
				write_log("CAL_ERROR\n");
				pglobal->commd_type = EMPTY;
				break;													 
			case RELAY_STATUS:
				sprintf(pub_topic, "%s%s", PUB_TOPIC_HEAD, pglobal->topic_tail_once); //拼接发送主题
				memset(data, 0, sizeof(data));
				data_len = read_data(fd, pglobal->commd_once, COMMD_SIZE, data);

				for (j = 0; j < data_len; j++)
				{
					printf("%02x ", data[j]);
				}
				printf("\n");
				pubmsg.payload = data;
				pubmsg.payloadlen = data_len;
				
				pglobal->commd_type = (pglobal->commd == NULL) ? EMPTY : SENSOR_DATA;
				break;
			case RELAY_CONTROL:
				sprintf(pub_topic, "%s%s", PUB_TOPIC_HEAD, pglobal->topic_tail_once); //拼接发送主题
				memset(data, 0, MAXLEN);
				
				//先发送控制指令，不接收返回的无用数据
				write_only(fd, pglobal->commd_once, COMMD_SIZE);
				//再发送读取状态的命令，获取继电器状态
				data_len = read_data(fd, pglobal->commd_once + COMMD_SIZE, COMMD_SIZE, data);
				if (data == NULL)
				{
					write_log("read_data error");
				}

				for (j = 0; j < data_len; j++)
				{
					printf("%02x ", data[j]);
				}
				printf("\n");
				for (i = 0; i < 8; i++)
				{
					(data[0] & key[i]) ? printf("1 ") : printf("0 ");
				}
				for (i = 0; i < 8; i++)
				{
					(data[1] & key[i]) ? printf("1 ") : printf("0 ");
				}
				printf("\n");
				pubmsg.payload = data;
				pubmsg.payloadlen = data_len;
				
				pglobal->commd_type = (pglobal->commd == NULL) ? EMPTY : SENSOR_DATA;
				break;
			case LOCAL_CONTROL:
				printf("LOCAL_CONTROL\n");
				message_len = 0;
				memset(data, 0, MAXLEN);
				for (i = 0; i < pglobal->all_data_count; i++)
				{
					if (pglobal->local_ctrl[i].flag == DATA_UNUSUAL)
					{
						for (j = 0; j < pglobal->local_ctrl[i].count_query; j++)
						{
							data_len = read_data(fd, pglobal->local_ctrl[i].cmmd_query + j * COMMD_SIZE, COMMD_SIZE, data + message_len);
							message_len += data_len;
						}

						for (j = 0; j < message_len; j++)
						{
							printf("%02x ", *(data + j));
						}
						printf("\n");
						sprintf(pub_topic, "%s%s", PUB_TOPIC_HEAD, pglobal->local_ctrl[i].topic_tail); //拼接发送主题
						pglobal->local_ctrl[i].flag = DEFINED;
						break;
					}
				}
				if (i == pglobal->all_data_count)
				{					
					pglobal->commd_type = (pglobal->commd == NULL) ? EMPTY : SENSOR_DATA;

					pthread_mutex_unlock(&pglobal->lock);
					continue;
				}
				else
				{
					pubmsg.payload = data;
					pubmsg.payloadlen = message_len;
					break;
				}
			default:
				write_log("commtype error\n");
				exit(0);
				break;
		}
		//printf("tail: %s\n", pglobal->topic_tail);
		pthread_mutex_unlock(&pglobal->lock);
		if ((rc = MQTTAsync_sendMessage(pglobal->sub_client, pub_topic, &pubmsg, &opts)) != MQTTASYNC_SUCCESS)
		{
			printf("Failed to start sendMessage, return code %d\n", rc);
		}
	}
	return NULL;
}

void pub_run(globals *arg)
{
	if (arg == NULL)
	{
		printf("error!! sub_run don't get arg!\n");
		exit(EXIT_FAILURE);
	}
	pglobal = arg;
	fd = init_dev();
	
	if (0 != pthread_mutex_init(&pglobal->lock, NULL))
	{
		write_log("pthread_cond_init error");
		exit(1);
	}

	//创建发布线程
	if (0 != pthread_create(&pub_thread_id, NULL, pub_thread, NULL))
	{
		write_log("pthread_create error");
		exit(EXIT_FAILURE);
	}
	pthread_detach(pub_thread_id);
	
	if (0 != pthread_create(&pub_data_thread_id, NULL, pub_data_thread, NULL))
	{
		write_log("pthread_create error");
		exit(EXIT_FAILURE);
	}
	pthread_detach(pub_data_thread_id);
}

int pub_exit(void)
{
	int ret;
	ret = pthread_cancel(pub_thread_id);
	if (ret != 0)
	{
		write_log("pthread_cancel error\n");
		return ret;
	}
	ret = pthread_cancel(pub_data_thread_id);
	if (ret != 0)
	{
		write_log("pthread_cancel error\n");
		return ret;
	}
	return ret;
}
