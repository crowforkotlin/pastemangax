#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include "base16384.hpp"

#define execute(function){
    const char *inputFileDir = env->GetStringUTFChars(sf, JNI_FALSE);
    const char *outputFileDir = env->GetStringUTFChars(df, JNI_FALSE); 
    char* encbuf = (char*)malloc(BASE16384_ENCBUFSZ);                
    char* decbuf = (char*)malloc(BASE16384_DECBUFSZ);                  
    int re = function(inputFileDir, outputFileDir, encbuf, decbuf);  
    free(encbuf); free(decbuf);                      
    env->ReleaseStringUTFChars(sf, inputFileDir);
    env->ReleaseStringUTFChars(df, outputFileDir);
    return re;
}

#define exe_byte(x) {
    jsize len = env->GetArrayLength(buf);
    const char* data = (char*)env->GetByteArrayElements(buf, JNI_FALSE);
    int x##len = base16384_##x##_len(len) + 64;
    char* x##buf = (char*) malloc(x##len);
    x##len = base16384_##x(data, len, x##buf, x##len);
    jbyteArray out = env->NewByteArray(x##len);
    env->SetByteArrayRegion(out, 0, x##len, reinterpret_cast<const jbyte *>(x##buf));
    free(x##buf);
    return out;
}

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_encode(JNIEnv* env, jobject, jstring sf, jstring df) execute(base16384_encode_file)

extern "C" JNIEXPORT int JNICALL
Java_top_fumiama_base16384_MainActivity_decode(JNIEnv* env, jobject, jstring sf, jstring df) execute(base16384_decode_file)

extern "C" JNIEXPORT jbyteArray JNICALL
        Java_top_fumiama_base16384_MainActivity_encodeByteArray(JNIEnv* env, jobject, jbyteArray buf) exe_byte(encode)

extern "C" JNIEXPORT jbyteArray JNICALL
        Java_top_fumiama_base16384_MainActivity_decodeByteArray(JNIEnv* env, jobject, jbyteArray buf) exe_byte(decode)