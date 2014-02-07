# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := monitor
LOCAL_SRC_FILES += \
    inotify-jni.cpp \
    FileObserver.cpp \
    EchoTcpServer.cpp \
    SimpleTcpClient.cpp \
    FileDeleteObserver.cpp \
    event_queue.c \

#NDK_ROOT := /usr/local/install/android-ndk-r8e
NDK_ROOT := C:/cygwin/home/Administrator/android-ndk-r8e
THIRD_PARTY_ROOT := ./third_party

LOCAL_LDLIBS += \
    -llog \
    -lz \
    -L$(NDK_ROOT)/sources/cxx-stl/stlport/libs/armeabi-v7a -lstlport_static \
    -L$(THIRD_PARTY_ROOT)/curl-7.34.0/lib -lcurl


LOCAL_C_INCLUDES += \
    $(NDK_ROOT)/sources/cxx-stl/stlport/stlport \
    $(THIRD_PARTY_ROOT)/curl-7.34.0/include

include $(BUILD_SHARED_LIBRARY)
LOCAL_PATH := $(call my-dir)
