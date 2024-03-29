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

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_CPP_EXTENSION := .cpp .cc
LOCAL_MODULE    := bar-updater
LOCAL_SRC_FILES := com_android_systemui_statusbar_phone_BarBackgroundUpdaterNative.cpp

LOCAL_LDLIBS    := -llog
LOCAL_LDLIBS    += $(LOCAL_PATH)/libsurfaceflinger_client.so $(LOCAL_PATH)/libbinder.so $(LOCAL_PATH)/libutils.so

include $(BUILD_SHARED_LIBRARY)



