@file:Suppress("SpellCheckingInspection", "AnnotateVersionCheck", "unused")

package com.crow.mangax.ui.text

import android.annotation.SuppressLint
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.crow.mangax.ui.text.AttrTextLayout
import com.crow.mangax.ui.text.IAttrText
import java.lang.reflect.Constructor


internal inline fun IAttrText.drawY(onTop: () -> Unit, onBottom: () -> Unit) {
    if (mTextAnimationTopEnable) onTop() else onBottom()
}

internal inline fun IAttrText.drawX(onLeft: () -> Unit, onRight: () -> Unit) {
    if (mTextAnimationLeftEnable) onLeft() else onRight()
}

internal inline fun debug(onDebug: () -> Unit) {
    if (IAttrText.DEBUG) onDebug()
}

internal inline fun debugText(onDebug: () -> Unit, orElse: () -> Unit) {
    if (IAttrText.DEBUG_TEXT) onDebug() else orElse()
}

internal inline fun debugAnimation(onDebug: () -> Unit) {
    if (IAttrText.DEBUG_ANIMATION) onDebug()
}

internal inline fun withPath(path: Path, pathOperations: Path.() -> Unit) {
    path.reset()
    path.pathOperations()
    path.close()
}

internal inline fun withApiO(leastO: () -> Unit, lessO: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) leastO() else lessO()
}

internal inline fun IAttrText.withSizeUnit(pxOrDefault: () -> Float, dpOrSp: () -> Float): Float {
    return if (mTextSizeUnitStrategy == AttrTextLayout.STRATEGY_DIMENSION_DP_OR_SP) dpOrSp() else pxOrDefault()
}

/**
 * ⦁ 错误输出
 *
 * ⦁ 2023-12-28 15:27:47 周四 下午
 * @author crowforkotlin
 */
internal fun Any?.errorLog(tag: String = IAttrText.TAG) {
    Log.e(tag, this.toString())
}

internal inline fun Handler.asyncMessage(delay: Long, runnable: Runnable, config: Message.() -> Unit = { }) {
    sendMessageDelayed(Message.obtain(this, runnable).also {
        it.config()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            it.isAsynchronous = true
        }
    }, delay)
}

internal fun Handler.asyncMessage(runnable: Runnable) {
    sendMessage(Message.obtain(this, runnable).also {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            it.isAsynchronous = true
        }
    })
}
internal fun Handler.sendMessage(runnable: Runnable, config: Message.() -> Unit) {
    sendMessage(Message.obtain(this, runnable).also { it.config() })
}

@SuppressLint("ObsoleteSdkInt")
internal fun Looper.asHandler(async: Boolean): Handler {
    // Async support was added in API 16.
    if (!async || Build.VERSION.SDK_INT < 16) { return Handler(this) }

    if (Build.VERSION.SDK_INT >= 28) {
//         TODO compile against API 28 so this can be invoked without reflection.
//        val factoryMethod = Handler::class.java.getDeclaredMethod("createAsync", Looper::class.java)
//        return factoryMethod.invoke(null, this) as Handler
        return Handler.createAsync(this)
    }

    val constructor: Constructor<Handler>
    try {
        constructor = Handler::class.java.getDeclaredConstructor(Looper::class.java,
            Handler.Callback::class.java, Boolean::class.javaPrimitiveType)
    } catch (ignored: NoSuchMethodException) {
        // Hidden constructor absent. Fall back to non-async constructor.
        return Handler(this)
    }
    return constructor.newInstance(this, null, true)
}