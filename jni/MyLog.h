#include <android/log.h>

#define DEBUG_MOD       0

#if DEBUG_MOD
#define XLOG(FORMAT,...) \
    { __android_log_print(ANDROID_LOG_DEBUG, "XLOG", FORMAT, ##__VA_ARGS__); }

#else
#define XLOG(FORMAT,...) 

#endif

