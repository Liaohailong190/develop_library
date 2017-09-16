#include <org_liaohailong_pdftestapp_JNI.h>

JNIEXPORT jstring JNICALL Java_org_liaohailong_pdftestapp_JNI_getHelloWord(JNIEnv *env, jclass jobj){
    return (*env)->NewStringUTF(env,"Hello Word!");
}

JNIEXPORT jint JNICALL Java_org_liaohailong_pdftestapp_JNI_addCalc(JNIEnv *env, jclass jobj, jint ja, jint jb) {
  return ja + jb;
}