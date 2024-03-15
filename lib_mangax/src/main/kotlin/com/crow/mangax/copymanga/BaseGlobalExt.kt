package com.crow.mangax.copymanga

import android.annotation.SuppressLint
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
import androidx.lifecycle.LifecycleCoroutineScope
import com.crow.base.app.app
import com.crow.mangax.R
import com.crow.base.R as baseR
import com.crow.mangax.copymanga.resp.BaseContentInvalidResp
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.px2dp
import com.crow.base.tools.extensions.showSnackBar
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.BaseViewState
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.mangax.tools.language.ChineseConverter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDivider
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/project_now
 * @Time: 2023/3/15 14:35
 * @Author: CrowForKotlin
 * @Description: UtilsExt
 * @formatter:on
 **************************/

// 漫画卡片高度 和 宽度
val appComicCardHeight: Int by lazy {
    val width = app.resources.displayMetrics.widthPixels
    val height = app.resources.displayMetrics.heightPixels
    (width.toFloat() / (3.0 - width.toFloat() / height.toFloat())).toInt()
}
val appComicCardWidth: Int by lazy { (appComicCardHeight / 1.25).toInt() }
val appDp10 by lazy { app.px2dp(app.resources.getDimensionPixelSize(baseR.dimen.base_dp10).toFloat()).toInt() }
val appEvent = BaseEvent.newInstance(BaseEvent.BASE_FLAG_TIME_1000 shl 1)

private val formatter  = DecimalFormat("###,###.##", DecimalFormatSymbols(Locale.US).also { it.groupingSeparator = '.' })
/**
 * ⦁ 格式化热度字符串
 *
 * ⦁ 2023-12-14 21:31:10 周四 下午
 * @author crowforkotlin
 */
@SuppressLint("DefaultLocale")
fun formatHotValue(value: Int): String {
    return if(!CatlogConfig.mHotAccurateDisplay)  {
        return when {
            value >= 10_000_000 -> {
                String.format("%.1fW", value / 10_000.0)
            }
            value >= 1_000_000 -> {
                String.format("%.1fW", value / 10_000.0)
            }
            value >= 100_000 -> {
                String.format("%.1fW", value / 10_000.0)
            }
            value >= 10_000 -> {
                String.format("%.1fW", value / 10_000.0)
            }
            value >= 1000 -> {
                String.format("%.1fK", value / 1000.0)
            }
            else -> value.toString()
        }
    } else {
        when {
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
}

/**
 * ⦁ 可扩展字符串
 *
 * ⦁ 2023-12-14 21:30:25 周四 下午
 * @author crowforkotlin
 */
fun String.getSpannableString(color: Int, start: Int, end: Int = length): SpannableString {
    return SpannableString(this).also { it.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
}

/**
 * ⦁ 处理Token错误
 *
 * ⦁ 2023-12-14 21:27:10 周四 下午
 * @author crowforkotlin
 */
fun View.processTokenError(code: Int, msg: String?, doOnCancel: (MaterialAlertDialogBuilder) -> Unit = { }, doOnConfirm: (MaterialAlertDialogBuilder) -> Unit) {
    runCatching { toTypeEntity<BaseContentInvalidResp>(msg)?.mResults ?: throw JsonDataException("parse exception!") }
        .onSuccess {
            context.newMaterialDialog { dialog ->
                dialog.setCancelable(false)
                val linear = LinearLayoutCompat(context)
                val divider = MaterialDivider(context)
                val textView = TextView(context)
                val dp10 = resources.getDimensionPixelSize(baseR.dimen.base_dp10)
                linear.layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                linear.orientation = LinearLayoutCompat.VERTICAL
                divider.layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.px2dp(resources.getDimensionPixelSize(baseR.dimen.base_dp1).toFloat()).toInt()).also {
                    it.setMargins(0, dp10, 0, 0)
                }
                textView.text = context.getString(R.string.mangax_token_error)
                textView.textSize = 18f
                textView.setPadding(context.resources.getDimensionPixelSize(baseR.dimen.base_dp20))
                textView.typeface = Typeface.DEFAULT_BOLD
                textView.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                linear.addView(divider)
                linear.addView(textView)
                dialog.setView(linear)
                dialog.setTitle(context.getString(R.string.mangax_tips))
                dialog.setPositiveButton(context.getString(R.string.mangax_confirm)) { _, _ -> doOnConfirm(dialog) }
                dialog.setNegativeButton(context.getString(R.string.mangax_cancel)) { _, _ -> doOnCancel(dialog) }
            }
        }
        .onFailure {
            if (code == BaseViewState.Error.UNKNOW_HOST) this.showSnackBar(msg ?: app.getString(baseR.string.base_loading_error))
            else toast(app.getString(R.string.mangax_unknow_error))
        }
}

inline fun LifecycleCoroutineScope.tryConvert(text: String, crossinline result: (String) -> Unit) {
   if (CatlogConfig.mChineseConvert) { launch { result(ChineseConverter.convert(text)) } } else result(text)
}

fun getImageUrl(url: String): String {
    return if ((AppConfig.getInstance()?.mApiSecret?.length ?: 0) >= 20 && CatlogConfig.mApiImageProxyEnable) {
        val httpUrl = url.toHttpUrl()
        httpUrl.newBuilder()
            .host(BaseStrings.URL.WUYA_API_IMAGE)
            .scheme(BaseStrings.URL.SCHEME_HTTPS)
            .addQueryParameter("image_host", httpUrl.host)
            .build()
            .toString()
    } else {
        url
    }
}