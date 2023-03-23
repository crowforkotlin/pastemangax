package com.crow.base.current_project

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import br.com.simplepass.loadingbutton.customViews.ProgressButton
import com.crow.base.R
import com.crow.base.app.appContext
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.ViewState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
private val comic_card_height: Int by lazy {
    val width = appContext.resources.displayMetrics.widthPixels
    val height = appContext.resources.displayMetrics.heightPixels
    (width.toFloat() / (3 - width.toFloat() / height.toFloat())).toInt()
}

fun getComicCardHeight() = comic_card_height
fun getComicCardWidth() = (comic_card_height / 1.25).toInt()

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

inline fun View.processTokenError(
    code: Int, msg: String?,
    crossinline doOnCancel: (MaterialAlertDialogBuilder) -> Unit = { },
    crossinline doOnConfirm: (MaterialAlertDialogBuilder) -> Unit
) {
    runCatching { msg.toTypeEntity<BaseTokenInvalidResp>()!!.mResults }
        .onSuccess {
            context.newMaterialDialog { dialog ->
                dialog.setCancelable(false)
                dialog.setView(TextView(context).also {  textView ->
                    textView.text = context.getString(R.string.BaseTokenError)
                    textView.textSize = 18f
                    textView.setPadding(context.resources.getDimensionPixelSize(R.dimen.base_dp20))
                    textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    textView.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                })
                dialog.setTitle(context.getString(R.string.BaseTips))
                dialog.setPositiveButton(context.getString(R.string.BaseConfirm)) { _, _ -> doOnConfirm(dialog) }
                dialog.setNegativeButton(context.getString(R.string.BaseCancel)) { _, _ -> doOnCancel(dialog) }
            }
        }
        .onFailure {
            if (code == ViewState.Error.UNKNOW_HOST) this.showSnackBar(msg ?: appContext.getString(R.string.BaseLoadingError))
            else toast(appContext.getString(R.string.BaseUnknow))
        }
}