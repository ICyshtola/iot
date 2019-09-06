#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/time.h>
#include "MQTTAsync.h"
#include "sub.h"
#include "log.h"

static globals *pglobal;
int disc_finished = 0;
int subscribed = 0;

void sub_onDisconnect(void* context, MQTTAsync_successData* response)
{
	//printf("Successful disconnection\n");
	write_log("Successful disconnection\n");
	disc_finished = 1;
}

void onSubscribe(void* context, MQTTAsync_successData* response)
{
	//printf("Subscribing to topic %s for client %s using QoS%d\n", SUB_TOPIC, SUB_CLIENTID, SUB_QOS);
	write_log("Subscribing to topic %s for client %s using QoS%d\n", SUB_TOPIC, SUB_CLIENTID, SUB_QOS);
	subscribed = 1;
}

void onSubscribeFailure(void* context, MQTTAsync_failureData* response)
{
	//printf("Subscribe failed, rc %d\n", response ? response->code : 0);
	write_log("Subscribe failed, rc %d\n", response ? response->code : 0);
}


void sub_onConnectFailure(void* context, MQTTAsync_failureData* response)
{
	//printf("Connect failed, rc %d\n", response ? response->code : 0);
	write_log("Connect failed, rc %d\n", response ? response->code : 0);
}

void sub_onConnect(void* context, MQTTAsync_successData* response)
{
	MQTTAsync client = (MQTTAsync)context;
	MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
	int rc;

	//printf("Connect Success\n");
	write_log("%s\n", "connect Success");

	opts.onSuccess = onSubscribe;
	opts.onFailure = onSubscribeFailure;
	opts.context = client;

	if ((rc = MQTTAsync_subscribe(client, SUB_TOPIC, SUB_QOS, &opts)) != MQTTASYNC_SUCCESS)
	{
		//printf("Failed to start subscribe, return code %d\n", rc);
		write_log("Failed to start subscribe, return code %d\n", rc);
		exit(EXIT_FAILURE);
	}
}

void sub_connlost(void *context, char *cause)
{
	//printf("\nConnection lost\n");
	write_log("Connection lost\n");
	if (cause)
	{
		//printf("     cause: %s\n", cause);
		write_log("cause: %s\n", cause);
	}
}

int msgarrvd(void *context, char *topicName, int topicLen, MQTTAsync_message *message)
{
    int i, tail_len, len, j;
	char *p_tail = topicName, *p_payload = message->payload, *temp = NULL;
	char *cal;

    printf("Message arrived\n");
    printf("     topic: %s\n", topicName);
    printf("   message: ");
	for (i = 0; i < message->payloadlen; i++)
	{
		printf("%02x ", p_payload[i]);
	}
	printf("\n");

	//加锁
	pthread_mutex_lock(&pglobal->lock);
	
	//提取主题尾部
	for (i = 0; i < 2; i++)
	{
		p_tail = strchr(p_tail, '/');
		if (p_tail == NULL)
		{
			printf("TOPIC error\n");
			//return 0;
			goto exit_msgarrvd;
		}
		p_tail++;
	}
	tail_len = strlen(p_tail);	
	//if (tail_len > MAXLEN - strlen(PUB_TOPIC_HEAD))
	if (tail_len > MAXLEN)
	{
		//printf("topic is too long\n");
		write_log("message topic is too long\n");
		//return 1;
		goto exit_msgarrvd;
	}
	if (0 == strncmp(p_tail, "DEBUG", 5))
	{
		write_log("Message Type: DEBUG\n");
		if (message->payloadlen != COMMD_SIZE)
		{
			write_log("message len error\n");
			goto exit_msgarrvd;
		}

		if (pglobal->commd_once != NULL)
		{
			free(pglobal->commd_once);
			pglobal->commd_once = NULL;
		}
		pglobal->commd_once = (unsigned char *)malloc(COMMD_SIZE);
		if (pglobal->commd_once == NULL)
		{
			write_log("malloc error\n");
			goto exit_msgarrvd;
		}
		memcpy(pglobal->commd_once, message->payload, message->payloadlen);

		strncpy(pglobal->topic_tail_once, p_tail, tail_len);
		pglobal->commd_type = DEBUG;
	}
	else if (0 == strncmp(p_tail, "SENSOR_DATA_COUNT", 17))
	{
		//printf("SENSOR_DATA_COUNT\n");
		write_log("Message Type: SENSOR_DATA_COUNT\n");
		p_payload = message->payload;
		pglobal->all_data_count = 0;
		for (i = 0; i < message->payloadlen / sizeof(int); i++)
		{
			memcpy(pglobal->sensor_data_count + i, p_payload + i * sizeof(int), sizeof(int));
			printf("%d ", pglobal->sensor_data_count[i]);
			pglobal->all_data_count += pglobal->sensor_data_count[i];
		}
		printf("\n");

		//为本地控制分配内存
		if (pglobal->local_ctrl != NULL)
		{
			free(pglobal->local_ctrl);
			pglobal->local_ctrl = NULL;
		}
		pglobal->local_ctrl = (local_control *)malloc(pglobal->all_data_count * sizeof(local_control));
		if (pglobal->local_ctrl == NULL)
		{
			//printf("malloc error\n");
			write_log("malloc error\n");
			goto exit_msgarrvd;
		}
		memset(pglobal->local_ctrl, 0, pglobal->all_data_count * sizeof(local_control));
	}
	else if (0 == strncmp(p_tail, "SENSOR_DATA", 11))
	{
		//printf("SENSOR\n");
		write_log("Message Type: SENSOR_DATA\n");
		for (i = 0; i < message->payloadlen; i++)
		{
			printf("%02x ", *(unsigned char *)(message->payload + i));
		}
		printf("\n");
		
		if (message->payloadlen % COMMD_SIZE != 0)
		{
			//printf("commd error\n");
			write_log("commd error\n");
			goto exit_msgarrvd;
		}
		
		//保存主题尾部
		strncpy(pglobal->topic_tail, p_tail, tail_len);
		pglobal->topic_tail[tail_len] = '\0';

		//提取消息（命令）
		pglobal->commd_count = message->payloadlen / COMMD_SIZE;
		if (pglobal->commd != NULL)
		{
			free(pglobal->commd);
			pglobal->commd = NULL;
		}
		pglobal->commd = (unsigned char *)malloc(message->payloadlen);
		if (pglobal->commd == NULL)
		{
			//perror("malloc error");
			write_log("malloc error\n");;
			goto exit_msgarrvd;
		}
		memcpy(pglobal->commd, message->payload, message->payloadlen);
		
		pglobal->commd_type = SENSOR_DATA;
		//send_message(pglobal->sub_client);
	}
	else if (0 == strncmp(p_tail, "SENSOR_CYCLE", 12))
	{
		//printf("CYCLE\n");
		write_log("Message Type: CYCLE\n");
		if (message->payloadlen < sizeof(int))
		{
			//printf("cycle error\n");
			write_log("cycle error\n");
			goto exit_msgarrvd;
		}
		memcpy(&pglobal->cycle, message->payload, sizeof(int));
		printf("cycle = %d\n", pglobal->cycle);
	}
	else if (0 == strncmp(p_tail, "RELAY_STATUS", 12))
	{
		//printf("RELAY_STATUS\n");
		write_log("Message Type: RELAY_STATUS\n");
		for (i = 0; i < message->payloadlen; i++)
		{
			printf("%02x ", *(unsigned char *)(message->payload + i));
		}
		printf("\n");

		if (message->payloadlen != COMMD_SIZE)
		{
			//printf("commd error\n");
			write_log("commd error\n");
			goto exit_msgarrvd;
		}
		
		//保存主题尾部
		strncpy(pglobal->topic_tail_once, p_tail, tail_len);
		pglobal->topic_tail_once[tail_len] = '\0';

		//提取消息（命令）
		pglobal->commd_once_count = message->payloadlen / COMMD_SIZE;
		if (pglobal->commd_once != NULL)
		{
			free(pglobal->commd_once);
			pglobal->commd_once = NULL;
		}
		pglobal->commd_once = (unsigned char *)malloc(message->payloadlen);
		if (pglobal->commd_once == NULL)
		{
			//perror("malloc error");
			write_log("malloc error");
			goto exit_msgarrvd;
		}
		memcpy(pglobal->commd_once, message->payload, message->payloadlen);
		
		pglobal->commd_type = RELAY_STATUS;
	}
	else if (0 == strncmp(p_tail, "RELAY_CONTROL", 13))
	{
		//printf("RELAY_CONTROL\n");
		write_log("Message Type: RELAY_CONTROL\n");
		for (i = 0; i < message->payloadlen; i++)
		{
			printf("%02x ", *(unsigned char *)(message->payload + i));
		}
		printf("\n");

		if (message->payloadlen != COMMD_SIZE * 2)
		{
			//printf("commd error\n");
			write_log("commd error\n");
			goto exit_msgarrvd;
		}
		
		//保存主题尾部
		strncpy(pglobal->topic_tail_once, p_tail, tail_len);
		pglobal->topic_tail_once[tail_len] = '\0';
		
		//保存命令
		pglobal->commd_once_count = message->payloadlen / COMMD_SIZE;
		if (pglobal->commd_once != NULL)
		{
			free(pglobal->commd_once);
			pglobal->commd_once = NULL;
		}
		pglobal->commd_once = (unsigned char *)malloc(message->payloadlen);
		if (pglobal->commd_once == NULL)
		{
			//perror("malloc error");
			write_log("malloc error");
			goto exit_msgarrvd;
		}
		memcpy(pglobal->commd_once, message->payload, message->payloadlen);
		pglobal->commd_type = RELAY_CONTROL;
	}
	else if (0 == strncmp(p_tail, "SENSOR_CAL", 10))
	{
		//printf("SENSOR_CAL\n");
		write_log("Message Type: SENSOR_CAL\n");
		for (i = 0; i < message->payloadlen; i++)
		{
			printf("%c", *((char *)(message->payload + i)));
		}
		printf("\n");

		cal = strchr(message->payload, '_');
		i = 0;
		while(cal != NULL)
		{
			cal++;
			cal = strchr(cal, '_');
			i++;
		}
		if (i != pglobal->all_data_count)
		{
			pglobal->commd_type = CAL_ERROR;
		}

		if (pglobal->cal_str != NULL)
		{
			free(pglobal->cal_str);
			pglobal->cal_str = NULL;
		}
		pglobal->cal_str = (char *)malloc(message->payloadlen + 1);
		if (pglobal->cal_str == NULL)
		{
			//perror("malloc error");
			write_log("malloc error");
			goto exit_msgarrvd;
		}
		strncpy(pglobal->cal_str, message->payload, message->payloadlen);
		pglobal->cal_str[message->payloadlen] = '\0';
	}
	else if (0 == strncmp(p_tail, "LOCAL_CONTROL", 13))
	{
		//printf("LOCAL_CONTROL\n");
		write_log("Message Type: LOCAL_CONTROL\n");
		for (i = 0; i < message->payloadlen; i++)
		{
			printf("%02x ", *(unsigned char *)(message->payload + i));
		}
		printf("\n");

		if (message->payloadlen < LOCAL_CONTROL_BASIC_SIZE)
		{
			//printf("LOCAL_CONTROL message error\n");
			write_log("LOCAL_CONTROL message error\n");
			goto exit_msgarrvd;
		}

		if (pglobal->local_ctrl == NULL)
		{
			//printf("local_ctrl is NULL\n");
			write_log("local_ctrl is NULL\n");
			goto exit_msgarrvd;
		}
		p_payload = message->payload;

		//解析是为第几个数据添加规则
		memcpy(&i, p_payload,  sizeof(int));
		p_payload += sizeof(int);

		printf("i = %d\n", i);
		if (i > pglobal->all_data_count)
		{
			//printf("LOCAL_CONTROL error! i is to big\n");
			write_log("LOCAL_CONTROL error! i is to big\n");
			goto exit_msgarrvd;
		}

		//保存数据最小值
		memcpy(&(pglobal->local_ctrl)[i].min, p_payload, sizeof(double));
		p_payload += sizeof(double);
		printf("min = %lf\n", pglobal->local_ctrl[i].min);

		//小于最小值时, 需要执行命令的个数
		memcpy(&pglobal->local_ctrl[i].count_min, p_payload, sizeof(int));
		p_payload += sizeof(int);
		len = LOCAL_CONTROL_BASIC_SIZE + pglobal->local_ctrl[i].count_min * COMMD_SIZE;
		if (len > message->payloadlen)
		{
			//printf("LOCAL_CONTROL message error\n");
			write_log("LOCAL_CONTROL message error\n");
			goto exit_msgarrvd;
		}

		//保存小于最小值时需要执行的命令
		if ((pglobal->local_ctrl)[i].cmmd_min != NULL)
		{
			free(pglobal->local_ctrl[i].cmmd_min);
			pglobal->local_ctrl[i].cmmd_min = NULL;
		}
		pglobal->local_ctrl[i].cmmd_min = (unsigned char*)malloc(pglobal->local_ctrl[i].count_min * COMMD_SIZE);
		if (pglobal->local_ctrl[i].cmmd_min == NULL)
		{
			//printf("malloc error\n");
			write_log("malloc error\n");
			goto exit_msgarrvd;
		}
		memcpy(pglobal->local_ctrl[i].cmmd_min, p_payload, pglobal->local_ctrl[i].count_min * COMMD_SIZE);
		p_payload += pglobal->local_ctrl[i].count_min * COMMD_SIZE;

		//保存数据最大值
		memcpy(&pglobal->local_ctrl[i].max, p_payload, sizeof(double));
		p_payload += sizeof(double);
		printf("max = %lf\n", pglobal->local_ctrl[i].max);

		//当数据达到最大值时需要执行命令的个数
		memcpy(&pglobal->local_ctrl[i].count_max, p_payload, sizeof(int));
		p_payload += sizeof(int);
		printf("count_max = %d\n", pglobal->local_ctrl[i].count_max);

		len += pglobal->local_ctrl[i].count_max * COMMD_SIZE;
		if (len > message->payloadlen)
		{
			//printf("LOCAL_CONTROL message error\n");
			write_log("LOCAL_CONTROL message error\n");
			goto exit_msgarrvd;
		}

		//保存当数据达到最大值时需要执行的命令
		if (pglobal->local_ctrl[i].cmmd_max != NULL)
		{
			free(pglobal->local_ctrl[i].cmmd_max);
			pglobal->local_ctrl[i].cmmd_max = NULL;
		}
		pglobal->local_ctrl[i].cmmd_max = (unsigned char *)malloc(pglobal->local_ctrl[i].count_max * COMMD_SIZE);
		if (pglobal->local_ctrl[i].cmmd_max == NULL)
		{
			//perror("malloc error");
			write_log("malloc error");
			goto exit_msgarrvd;
		}
		memcpy(pglobal->local_ctrl[i].cmmd_max, p_payload, pglobal->local_ctrl[i].count_max * COMMD_SIZE);
		p_payload += pglobal->local_ctrl[i].count_max * COMMD_SIZE;

		for (j = 0; j < pglobal->local_ctrl[i].count_max * COMMD_SIZE; j++)
		{
			printf("%02x ", *(pglobal->local_ctrl[i].cmmd_max + j));
		}
		printf("\n");

		//保存需要执行的查询语句的个数
		memcpy(&pglobal->local_ctrl[i].count_query, p_payload, sizeof(int));
		p_payload += sizeof(int);
		printf("count_query = %d\n", pglobal->local_ctrl[i].count_query);
		len += pglobal->local_ctrl[i].count_query * COMMD_SIZE;
		if (len != message->payloadlen)
		{
			//printf("message error\n");
			write_log("message error\n");
			goto exit_msgarrvd;
		}

		//保存读取继电器状态的命令
		if (pglobal->local_ctrl[i].cmmd_query != NULL)
		{
			free(pglobal->local_ctrl[i].cmmd_query);
			pglobal->local_ctrl[i].cmmd_query = NULL;
		}
		pglobal->local_ctrl[i].cmmd_query = (unsigned char *)malloc(COMMD_SIZE * pglobal->local_ctrl[i].count_query);
		if (pglobal->local_ctrl[i].cmmd_query == NULL)
		{
			//perror("malloc error");
			write_log("malloc error");
			goto exit_msgarrvd;
		}
		memcpy(pglobal->local_ctrl[i].cmmd_query, p_payload, COMMD_SIZE * pglobal->local_ctrl[i].count_query);

		for (j = 0; j < COMMD_SIZE * pglobal->local_ctrl[i].count_query; j++)
		{
			printf("%02x ", *(pglobal->local_ctrl[i].cmmd_query + j));
		}
		printf("\n");

		strncpy(pglobal->local_ctrl[i].topic_tail, p_tail, tail_len);
		pglobal->local_ctrl[i].topic_tail[tail_len] = '\0';

		pglobal->local_ctrl[i].max_control_needed = 1;
		pglobal->local_ctrl[i].min_control_needed = 1;

		pglobal->local_ctrl[i].flag = DEFINED;	//标志计为1，表明此项数据有规则
	}
	//解锁

exit_msgarrvd:	pthread_mutex_unlock(&pglobal->lock);
				usleep(10000);
				MQTTAsync_freeMessage(&message);
				MQTTAsync_free(topicName);
				return 1;
}




void sub_disconnect(MQTTAsync client)
{
	int rc;
	MQTTAsync_disconnectOptions disc_opts = MQTTAsync_disconnectOptions_initializer;
	disc_opts.onSuccess = sub_onDisconnect;
	if ((rc = MQTTAsync_disconnect(client, &disc_opts)) != MQTTASYNC_SUCCESS)
	{
		//printf("Failed to start disconnect, return code %d\n", rc);
		write_log("Failed to start disconnect, return code %d\n", rc);
		exit(EXIT_FAILURE);
	}
 	while	(!disc_finished)
	{
		sleep(1);
	}
}

void sub_run(globals *arg)
{	
	MQTTAsync client;
	MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
	int rc;
	int ch;
	
	if (arg == NULL)
	{
		printf("error!! sub_run don't get arg!\n");
		exit(EXIT_FAILURE);
	}
	pglobal = arg;

	MQTTAsync_create(&client, SUB_ADDRESS, SUB_CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, NULL);
	pglobal->sub_client = client;

	MQTTAsync_setCallbacks(client, client, sub_connlost, msgarrvd, NULL);

	conn_opts.keepAliveInterval = 20;
	conn_opts.cleansession = 0;
	conn_opts.automaticReconnect = 1;
	conn_opts.onSuccess = sub_onConnect;
	conn_opts.onFailure = sub_onConnectFailure;
	conn_opts.context = client;
	if ((rc = MQTTAsync_connect(client, &conn_opts)) != MQTTASYNC_SUCCESS)
	{
		//printf("Failed to start connect, return code %d\n", rc);
		write_log("Failed to start connect, return code %d\n", rc);
		exit(EXIT_FAILURE);
	}
	while(!subscribed)
	{
		usleep(10000);
	}
}
