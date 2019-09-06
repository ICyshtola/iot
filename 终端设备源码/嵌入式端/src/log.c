#include <stdio.h>
#include <stdarg.h>
#include <time.h>
#include <pthread.h>

FILE *pFile = NULL;
pthread_mutex_t lock_log;

int write_log (const char *format, ...) {
    va_list arg;
    int done;

    va_start (arg, format);
    done = vfprintf (stdout, format, arg);

    time_t time_log = time(NULL);
    struct tm* tm_log = localtime(&time_log);
    fprintf(pFile, "%04d-%02d-%02d %02d:%02d:%02d ", tm_log->tm_year + 1900, tm_log->tm_mon + 1, tm_log->tm_mday, tm_log->tm_hour, tm_log->tm_min, tm_log->tm_sec);

	pthread_mutex_lock(&lock_log);
    done = vfprintf (pFile, format, arg);
    va_end (arg);

    fflush(pFile);
	pthread_mutex_unlock(&lock_log);
    return done;
}

FILE *init_log(void)
{
	pFile = fopen("log.log", "a");
	if (pFile == NULL)
	{
		perror("fopen error");
		return NULL;
	}
	if (0 != pthread_mutex_init(&lock_log, NULL))
	{
		perror("init_log_lock error");
		return NULL;
	}
	return pFile;
}

void close_log()
{
	pthread_mutex_lock(&lock_log);
	fclose(pFile);
	pthread_mutex_unlock(&lock_log);
}

/*int main() {
    pFile = init_log();
    write_log(pFile, "%s %d %f\n", "is running", 10, 55.55);
    close_log();

    return 0;
}*/
