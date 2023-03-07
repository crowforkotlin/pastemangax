package com.crow.base.extensions

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.FloatRange

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/dialog
 * @Time: 2022/5/11 21:38
 * @Author: BarryAllen
 * @Description: Dialog
 **************************/

inline fun Context.newDialog(dialogConfig: AlertDialog.Builder.() -> Unit) {
    AlertDialog.Builder(this).apply {
        dialogConfig()
        create()
        show()
    }
}

/* 重写onStart并在内部使用 */
fun Window.setLayoutWidthAndHeight(
    @FloatRange(from = 0.0, to = 1.0) width: Float,
    @FloatRange(from = 0.0, to = 1.0) height: Float,
) {
    val displayMetrics = context.resources.displayMetrics
    setLayout(
        (displayMetrics.widthPixels * width).toInt(),
        (displayMetrics.heightPixels * height).toInt()
    )
}


fun Window.setLayoutMatch() =
    setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

fun Window.setLayoutWarp() =
    setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

fun Window.setBackgroundTransparent() = setBackgroundDrawableResource(android.R.color.transparent)
fun Window.setMaskAmount(@FloatRange(from = 0.0, to = 100.0) amount: Float) = setDimAmount(amount)
