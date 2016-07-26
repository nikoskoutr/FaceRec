LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= /home/nikos/AndroidStudioProjects/FaceRec/app/install/
OPENCV_INSALL_MODULES:=on
OPENCV_CAMERA_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := com_apps_ktr_facerec_NativeClass.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := native

include $(BUILD_SHARED_LIBRARY)
