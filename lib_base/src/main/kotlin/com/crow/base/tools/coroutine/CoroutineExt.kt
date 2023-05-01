package com.crow.base.tools.coroutine

import com.crow.base.tools.extensions.logError
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/coroutine
 * @Time: 2022/5/5 9:02
 * @Author: CrowForKotlin
 * @Description: Coroutine Ext
 **************************/

val globalCoroutineException = GlobalCoroutineExceptionHandler()

class GlobalCoroutineExceptionHandler : CoroutineExceptionHandler {

    override val key: CoroutineContext.Key<*> get() = CoroutineExceptionHandler
    override fun handleException(context: CoroutineContext, exception: Throwable) { "Catch GlobalCoroutineException : ${exception.stackTraceToString()}".logError() }
}