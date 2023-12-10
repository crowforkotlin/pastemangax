@file:Suppress("unused")

package com.crow.base.tools.extensions

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.crow.base.app.app
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger

/*************************
 * @Machine: RedmiBook Pro 15
 * @RelativePath: cn\barry\base\extensions\TipExt.kt
 * @Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\lib_base\src\main\java\cn\barry\base\extensions\TipExt.kt
 * @Author: CrowForKotlin
 * @Time: 2022/5/1 20:58 周日 下午
 * @Description: Tips Ext
 * @formatter:off
 *************************/

const val TIPS_TAG = "CopyMangaX"

/* Original PRETTY LOGGER日志输出 */
fun String.logError() = Logger.e(this)
fun String.logDebug() = Logger.d(this)
fun String.logWarnign() = Logger.w(this)
fun String.logInfo() = Logger.i(this)
fun String.logVerbose() = Logger.v(this)

/* 三方依赖Logger 有反射 */
@Deprecated("此函数包含反射，不建议在正式版中大量使用")
fun Any?.preetyLogger(level :  Int = Logger.INFO){
    if (this == null) {
        Logger.e("The loogger value is null.")
        return
    }
    val type = this::class.java.simpleName
    when(level) {
        Logger.ERROR -> Logger.e("$type : $this")
        Logger.DEBUG -> Logger.d("$type : $this")
        Logger.WARN -> Logger.w("$type : $this")
        Logger.INFO -> Logger.i("$type : $this")
        Logger.VERBOSE -> Logger.v("$type : $this")
    }
}

/* Original Android Logger */
fun logger(content: Any?, level: Int = Logger.INFO, tag: String = TIPS_TAG) {
    if (content == null) {
        Log.e(tag, NullPointerException("Value is null.").stackTraceToString())
        return
    }
    when(level) {
        Logger.ERROR -> Log.e(tag,"$content")
        Logger.DEBUG -> Log.d(tag,"$content")
        Logger.WARN -> Log.w(tag,"$content")
        Logger.INFO -> Log.i(tag,"$content")
        Logger.VERBOSE -> Log.v(tag,"$content")
    }
}

/* Original Android Logger */
fun Any?.log(level: Int = Logger.INFO, tag: String = TIPS_TAG) {
    if (this == null) {
        Log.e(tag, NullPointerException("Value is null.").stackTraceToString())
        return
    }
    when(level) {
        Logger.ERROR -> Log.e(tag,"$this")
        Logger.DEBUG -> Log.d(tag,"$this")
        Logger.WARN -> Log.w(tag,"$this")
        Logger.INFO -> Log.i(tag,"$this")
        Logger.VERBOSE -> Log.v(tag,"$this")
    }
}


private var mToast: Toast? = null
private var mToastHide: Boolean = false

/* String Toast */
fun toast(msg: String, isShort: Boolean = true, context: Context = app.applicationContext) {
    if (mToastHide) return
    mToast?.cancel()
    mToast = Toast.makeText(context, msg, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
    mToast?.show()
}

/* CharSequence Toast */
fun toast(charSequence: CharSequence, isShort: Boolean = true) {
    if (mToastHide) return
    mToast?.cancel()
    mToast = Toast.makeText(app.applicationContext, charSequence, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
    mToast?.show()
}

/* SnackBar Show 字符串参数 */
fun View.showSnackBar(text: String, actionText: String? = null, duration: Int = Snackbar.LENGTH_SHORT, actionBlock: ((snackBar: Snackbar) -> Unit)? = null) {
    val snackBar = Snackbar.make(this, text, duration)
    if (actionText != null && actionBlock != null) snackBar.setAction(actionText) { actionBlock(snackBar) }
    snackBar.show()
}

/* SnackBar Show 资源ID参数 */
fun View.showSnackBar(resId: Int, actionResId: Int? = null, duration: Int = Snackbar.LENGTH_SHORT, actionBlock: ((snackbar: Snackbar) -> Unit)? = null) {
    val snackBar = Snackbar.make(this, resId, duration)
    if (actionResId != null && actionBlock != null) snackBar.setAction(actionResId) { actionBlock(snackBar) }
    snackBar.show()
}

fun error(message: Any): Nothing = throw IllegalStateException(message.toString())