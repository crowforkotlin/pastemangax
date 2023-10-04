package com.crow.base.copymanga

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import com.crow.base.R
import com.crow.base.app.app
import com.crow.base.tools.extensions.SpNameSpace
import com.crow.base.tools.extensions.getSharedPreferences
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.px2dp
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.BaseViewState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDivider
import com.squareup.moshi.JsonDataException
import java.text.DecimalFormat
import java.util.Locale

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

// 漫画卡片高度 和 宽度
val appComicCardHeight: Int by lazy {
    val width = app.resources.displayMetrics.widthPixels
    val height = app.resources.displayMetrics.heightPixels
    (width.toFloat() / (3.0 - width.toFloat() / height.toFloat())).toInt()
}
val appComicCardWidth: Int by lazy { (appComicCardHeight / 1.25).toInt() }
val appDp10 by lazy { app.px2dp(app.resources.getDimensionPixelSize(R.dimen.base_dp10).toFloat()).toInt() }
var appIsDarkMode = SpNameSpace.CATALOG_NIGHT_MODE.getSharedPreferences().getBoolean(SpNameSpace.Key.ENABLE_DARK, false)

/**
 * ● 处理Token 错误
 *
 * ● 2023-09-22 22:57:48 周五 下午
 */
inline fun View.processTokenError(code: Int, msg: String?, crossinline doOnCancel: (MaterialAlertDialogBuilder) -> Unit = { }, crossinline doOnConfirm: (MaterialAlertDialogBuilder) -> Unit) {
    runCatching { toTypeEntity<BaseContentInvalidResp>(msg)?.mResults ?: throw JsonDataException("parse exception!") }
        .onSuccess {
            context.newMaterialDialog { dialog ->
                dialog.setCancelable(false)
                val linear = LinearLayoutCompat(context)
                val divider = MaterialDivider(context)
                val textView = TextView(context)
                val dp10 = resources.getDimensionPixelSize(R.dimen.base_dp10)
                linear.layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                linear.orientation = LinearLayoutCompat.VERTICAL
                divider.layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.px2dp(resources.getDimensionPixelSize(R.dimen.base_dp1).toFloat()).toInt()).also {
                    it.setMargins(0, dp10, 0, 0)
                }
                textView.text = context.getString(R.string.BaseTokenError)
                textView.textSize = 18f
                textView.setPadding(context.resources.getDimensionPixelSize(R.dimen.base_dp20))
                textView.typeface = Typeface.DEFAULT_BOLD
                textView.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                linear.addView(divider)
                linear.addView(textView)
                dialog.setView(linear)
                dialog.setTitle(context.getString(R.string.BaseTips))
                dialog.setPositiveButton(context.getString(R.string.BaseConfirm)) { _, _ -> doOnConfirm(dialog) }
                dialog.setNegativeButton(context.getString(R.string.BaseCancel)) { _, _ -> doOnCancel(dialog) }
            }
        }
        .onFailure {
            if (code == BaseViewState.Error.UNKNOW_HOST) this.showSnackBar(msg ?: app.getString(R.string.BaseLoadingError))
            else toast(app.getString(R.string.BaseUnknowError))
        }
}


