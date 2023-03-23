package com.crow.base.tools.extensions

import android.content.Context
import android.content.Intent

/*************************
 * @ProjectName: JetpackApp
 * @Dir_Path: lib_base/src/main/java/cn/barry/base
 * @Time: 2022/3/8 10:37
 * @Author: CrowForKotlin
 * @Description: Activity Ext
 **************************/
inline fun <reified T> Context.startActivity() = startActivity(Intent(this, T::class.java))

inline fun <reified T> Context.startActivity(lambda: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.lambda()
    startActivity(intent)
}