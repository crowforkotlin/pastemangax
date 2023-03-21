package com.crow.base.current_project

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import br.com.simplepass.loadingbutton.customViews.ProgressButton
import com.crow.base.app.appContext
import java.text.DecimalFormat
import java.util.*

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/project_now
 * @Time: 2023/3/15 14:35
 * @Author: CrowForKotlin
 * @Description: UtilsExt
 * @formatter:on
 **************************/

private val formatter  = DecimalFormat.getInstance(Locale.US) as DecimalFormat

fun formatValue(value: Int): String {
    return when {
        value >= 10000 -> {
            formatter.applyPattern("#,#### W")
            formatter.format(value)
        }
        value >= 1000 -> {
            formatter.applyPattern("#,### K")
            formatter.format(value)
        }
        else -> value.toString()
    }
}

fun String.getSpannableString(color: Int, start: Int, end: Int = length): SpannableString {
    return SpannableString(this).also { it.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
}

// 漫画卡片高度
val ComicCardHeight: Int by lazy {
    val width = appContext.resources.displayMetrics.widthPixels
    val height = appContext.resources.displayMetrics.heightPixels
    (width.toFloat() / (3 - width.toFloat() / height.toFloat())).toInt()
}

// Fix Memory Leak
fun ProgressButton.updateLifecycleObserver(lifecycle: Lifecycle?) {
    getContext().removeLifecycleObserver(this) // to fix the leak.
    lifecycle?.addObserver(this) // to fix leaking after the fragment's view is destroyed.
}

// Fix Memory Leak
private fun Context.removeLifecycleObserver(observer: LifecycleObserver) {
    when (this) {
        is LifecycleOwner -> lifecycle.removeObserver(observer)
        is ContextThemeWrapper -> baseContext.removeLifecycleObserver(observer)
        is androidx.appcompat.view.ContextThemeWrapper -> baseContext.removeLifecycleObserver(observer)
    }
}