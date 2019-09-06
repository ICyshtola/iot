#ifndef LOG_H_
#define LOG_H_

/* 初始化日志，成功返回日志文件指针，失败返回NULL */
FILE *init_log(void);

/* 写日志,传入可变参数 */
int write_log (const char *format, ...);

/* 关闭日志文件 */
void close_log();

#endif
