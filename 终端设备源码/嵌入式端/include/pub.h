#ifndef PUB_H_
#define PUB_H_

#include "global.h"
#define PUB_TOPIC_HEAD       "UPLOAD/F4:5E:AB:57:F0:FF/"
#define PUB_QOS         0

/* 开启发布线程 */
void pub_run(globals *arg);

/* 关闭线程 */
int pub_exit(void);

#endif
