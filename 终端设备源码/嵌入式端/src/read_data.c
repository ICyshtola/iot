#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <termios.h>
#include <sys/time.h>
#include "read_data.h"
#include "global.h"
#include "log.h"

#define HEAD_SIZE 3
#define TAIL_SIZE 2

void tty_set(int fd)
{
    struct termios newtio;

    /* ignore modem control lines and enable receiver */
    memset(&newtio, 0, sizeof(newtio));
    newtio.c_cflag = newtio.c_cflag |= CLOCAL | CREAD;
    newtio.c_cflag = newtio.c_cflag |= CLOCAL | CREAD | CRTSCTS;
    newtio.c_cflag &= ~CSIZE;

    /* set character size */
    newtio.c_cflag |= CS8;

    /* set the parity */
    newtio.c_cflag &= ~PARENB;

    /* set the stop bits */
    newtio.c_cflag &= ~CSTOPB;

    /* set output and input baud rate */
    cfsetospeed(&newtio, B9600);
    cfsetispeed(&newtio, B9600);

    /* set timeout in deciseconds for non-canonical read */
    newtio.c_cc[VTIME] = 0;

    /* set minimum number of characters for non-canonical read */
    newtio.c_cc[VMIN] = 1;

    /* flushes data received but not read */
    tcflush(fd, TCIFLUSH);
    /* set the parameters associated with the terminal from
        the termios structure and the change occurs immediately */
    if((tcsetattr(fd, TCSANOW, &newtio))!=0) {
        write_log("set_tty/tcsetattr\n");
    }
}

int write_only(int fd, unsigned char *commd, int commd_len)
{
	int i, data_size = 0, ret, count = 0;
	unsigned char temp;
	fd_set fds;
	struct timeval time_out;

	for (i = 0; i < commd_len; i++)
	{
		printf("%02x ", *(commd + i));
	}
	printf("\n");
	/*if (commd_len < write(fd, commd, commd_len))
	{
		perror("write error");
		return 0;
	}*/
	for (i = 0; i < commd_len; i++)
	{
		if (1 < write(fd, commd + i, 1))
		{
			write_log("write error\n");
			return 0;
		}
	}
	
	for (i = 0; 1; i++)
	{
		FD_ZERO(&fds);
		FD_SET(fd, &fds);
		time_out.tv_sec = 0;
		time_out.tv_usec = 500000;

		ret = select(fd + 1, &fds, NULL, NULL, &time_out);
		if (ret < 0)
		{
			write_log("select error\n");
			return -1;
		}
		else if (ret == 0)
		{
			printf("time_out\n");
			break;
		}

		if (FD_ISSET(fd, &fds))
		{
			if (1 < read(fd, &temp, 1))
			{
				write_log("read error\n");
				return 0;
			}
			if (i == HEAD_SIZE - 1)
			{
				data_size = (int)temp;
			}
			printf("%02x ", temp);
		}
	}
	return data_size;
}

int read_all(int fd, unsigned char *commd, int commd_len, unsigned char *data)
{
	int i, ret, count = 0;
	fd_set fds;
	struct timeval time_out;

	for (i = 0; i < commd_len; i++)
	{
		printf("%02x ", *(commd + i));
	}
	printf("\n");
	/*if (commd_len < write(fd, commd, commd_len))
	{
		perror("write error");
		return 0;;
	}*/
	for (i = 0; i < commd_len; i++)
	{
		if (1 < write(fd, commd + i, 1))
		{
			write_log("write error\n");
			return 0;
		}
	}
	
	for (i = 0; 1; i++)
	{
		FD_ZERO(&fds);
		FD_SET(fd, &fds);
		time_out.tv_sec = 0;
		time_out.tv_usec = 500000;

		ret = select(fd + 1, &fds, NULL, NULL, &time_out);
		if (ret < 0)
		{
			write_log("select error\n");
			return 0;
		}
		else if (ret == 0)
		{
			printf("time_out\n");
			break;
		}

		if (FD_ISSET(fd, &fds))
		{
			if (1 < read(fd, data + i, 1))
			{
				write_log("read error\n");
				return 0;
			}
			printf("%02x ", *(data + i));
		}
	}
		
	return i;
}

int read_data(int fd, unsigned char *commd, int commd_len, unsigned char *data)
{
	int i, data_size = 0, ret, count = 0;
	unsigned char temp;
	fd_set fds;
	struct timeval time_out;

	for (i = 0; i < commd_len; i++)
	{
		printf("%02x ", *(commd + i));
	}
	printf("\n");
	/*if (commd_len < write(fd, commd, commd_len))
	{
		perror("write error");
		return 0;;
	}*/
	for (i = 0; i < commd_len; i++)
	{
		if (1 < write(fd, commd + i, 1))
		{
			write_log("write error\n");
			return 0;
		}
	}
	
	for (i = 0; 1; i++)
	{
		FD_ZERO(&fds);
		FD_SET(fd, &fds);
		time_out.tv_sec = 0;
		time_out.tv_usec = 500000;

		ret = select(fd + 1, &fds, NULL, NULL, &time_out);
		if (ret < 0)
		{
			write_log("select error\n");
			return 0;
		}
		else if (ret == 0)
		{
			printf("time_out\n");
			break;
		}

		if (FD_ISSET(fd, &fds))
		{
			if (1 < read(fd, &temp, 1))
			{
				write_log("read error\n");
				return 0;
			}
			printf("%02x ", temp);
			if (i == HEAD_SIZE - 1)
			{
				data_size = (int)temp;
				//data[count] = temp;
				//count++;
			}
			if (i >= HEAD_SIZE && i <= HEAD_SIZE + data_size)
			{
				data[count] = temp;
				count++;
			}
		}
	}
		
	return data_size;
}

int init_dev(void)
{
	int i, fd;
	fd = open("/dev/ttyO1", O_RDWR);
	if (fd < 0)
	{
		write_log("open /dev/ttyS0 error");
		return -1;
	}
	tty_set(fd);	//设置波特率等
	return fd;
}
