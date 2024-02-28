@file:Suppress("SpellCheckingInspection", "AnnotateVersionCheck")

package com.crow.mangax.ui.text

import android.graphics.Path
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
 * ● 错误输出
 *
 * ● 2023-12-28 15:27:47 周四 下午
 * @author crowforkotlin
 */
internal fun Any?.errorLog(tag: String = IAttrText.TAG) {
    Log.e(tag, this.toString())
}

internal fun CoroutineScope.scope(duration: Long = 0L, block: suspend () -> Unit) {
   launch {
       if(duration != 0L) delay(duration)
       block()
   }
}