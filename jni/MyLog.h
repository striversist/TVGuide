#include <android/log.h>

#define XLOG(FORMAT,...) \
    { __android_log_print(ANDROID_LOG_DEBUG, "XLOG", FORMAT, ##__VA_ARGS__); }

