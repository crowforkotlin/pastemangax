#include <jni.h>
#include <malloc.h>
#include "Converter.hpp"
#include <android/log.h>
#include "Config.hpp"
#include "SimpleConverter.hpp"

/**
 * 拆分路径
 */
extern "C"
jstring Java_com_crow_mangax_tools_language_ChineseConverter_convert(
        JNIEnv *env,
        jobject type,
        jstring text_, jstring configFile_,
        jstring absoluteDataFolderPath_
) {
    const char *text = env->GetStringUTFChars(text_, 0);
    const char *configFile = env->GetStringUTFChars(configFile_, 0);
    const char *absoluteDataFolderPath = env->GetStringUTFChars(absoluteDataFolderPath_, 0);

    opencc::SimpleConverter simpleConverter(std::string(absoluteDataFolderPath) + "/" + std::string(configFile));

    env->ReleaseStringUTFChars(text_, text);
    env->ReleaseStringUTFChars(configFile_, configFile);
    env->ReleaseStringUTFChars(absoluteDataFolderPath_, absoluteDataFolderPath);

    return env->NewStringUTF(simpleConverter.Convert(text).c_str());
}

/**
 *  统一路径
 */
extern "C"
jstring Java_com_crow_mangax_tools_language_ChineseConverter_nativeConvert(
        JNIEnv *env,
        jobject type,
        jstring text_, jstring configFile_
) {
    const char *text = env->GetStringUTFChars(text_, 0);
    const char *configFile = env->GetStringUTFChars(configFile_, 0);
    opencc::SimpleConverter simpleConverter((std::string(configFile)));
    env->ReleaseStringUTFChars(text_, text);
    env->ReleaseStringUTFChars(configFile_, configFile);
    return env->NewStringUTF(simpleConverter.Convert(text).c_str());
}