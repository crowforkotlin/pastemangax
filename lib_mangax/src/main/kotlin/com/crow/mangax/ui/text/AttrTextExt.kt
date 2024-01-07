@file:Suppress("SpellCheckingInspection", "AnnotateVersionCheck")

package com.crow.mangax.ui.text

import android.graphics.Path
import android.os.Build
import android.util.Log


inline fun IAttrText.drawY(onTop: () -> Unit, onBottom: () -> Unit) {
    if (mAnimationTop) onTop() else onBottom()
}

inline fun IAttrText.drawX(onLeft: () -> Unit, onRight: () -> Unit) {
    if (mAnimationLeft) onLeft() else onRight()
}

inline fun debug(onDebug: () -> Unit) {
    if (IAttrText.DEBUG) onDebug()
}

inline fun debugText(onDebug: () -> Unit, orElse: () -> Unit) {
    if (IAttrText.DEBUG_TEXT) onDebug() else orElse()
}

inline fun debugAnimation(onDebug: () -> Unit) {
    if (IAttrText.DEBUG_ANIMATION) onDebug()
}

inline fun withPath(path: Path, pathOperations: Path.() -> Unit) {
    path.reset()
    path.pathOperations()
    path.close()
}

inline fun withApiO(leastO: () -> Unit, lessO: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) leastO() else lessO()
}

inline fun IAttrText.withSizeUnit(px: () -> Float, orElse: () -> Float): Float {
    return if (mSizeUnitStrategy == AttrTextLayout.STRATEGY_DIMENSION_DP_SP) orElse() else px()
}

/**
 * ● 错误输出
 *
 * ● 2023-12-28 15:27:47 周四 下午
 * @author crowforkotlin
 */
fun Any?.errorLog(tag: String = IAttrText.TAG) {
    Log.e(tag, this.toString())
}