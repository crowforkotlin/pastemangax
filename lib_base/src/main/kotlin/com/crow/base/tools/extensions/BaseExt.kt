package com.crow.base.tools.extensions

import android.util.Log


fun String.removeWhiteSpace() = filterNot { it.isWhitespace() }

// 安全转换
fun<T : Any> safeAs(value: Any?): T? {
    return runCatching { value as T }.onFailure { logger("cast error!", Log.ERROR) }.getOrNull()
}