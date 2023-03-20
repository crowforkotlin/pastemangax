package com.crow.base.app

import com.crow.base.tools.extensions.logError


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/app
 * @Time: 2022/5/8 18:11
 * @Author: CrowForKotlin
 * @Description: BaseAppException
 **************************/
class BaseAppException : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        e.stackTraceToString().logError()
    }
}