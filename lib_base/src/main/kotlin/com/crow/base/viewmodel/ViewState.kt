package com.crow.base.viewmodel

import androidx.annotation.IntRange
import androidx.fragment.app.FragmentManager
import com.crow.base.dialog.LoadingAnimDialog

/*
@Machine: RedmiBook Pro 15
@RelativePath: cn\barry\base\viewmodel\ViewState.kt
@Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\lib_base\src\main\java\cn\barry\base\viewmodel\ViewState.kt
@Author: CrowForKotlin
@Time: 2022/4/26 9:46 周二 上午
@Description:
*/

sealed class ViewState {

    // 用于预构建
    object Default: ViewState()

    // 正在加载中
    object Loading : ViewState()

    // 加载成功
    class Success(@IntRange(from = 0L, to = 1L) val type: Int = NO_ATTACH_VALUE) : ViewState() {
        companion object {
            const val NO_ATTACH_VALUE = 0
            const val ATTACH_VALUE = 1
        }
    }

    // 加载失败
    class Error(val type: Int = -1, val msg: String? = null) : ViewState()


}

//自定义error 可以抛出来结束流的运行
class ViewStateException(msg: String, throwable: Throwable? = null) : Exception(msg, throwable)

class JsonFormatException(code: Int? = -1, msg: String? = null, throwable: Throwable? = null): Exception(msg, throwable)

class ResultCodeException(code: Int? = -1, msg: String? = null, throwable: Throwable? = null): Exception(msg, throwable)

inline fun ViewState.doOnResultWithLoading(fragmentManager: FragmentManager, crossinline onResult: () -> Unit) {
    doOnLoading { LoadingAnimDialog.show(fragmentManager) }
    doOnError { _, _ -> LoadingAnimDialog.dismiss(fragmentManager) }
    doOnSuccess { if (it == ViewState.Success.ATTACH_VALUE) { onResult() } else { LoadingAnimDialog.dismiss(fragmentManager) } }
}

inline fun ViewState.doOnLoading(crossinline block: () -> Unit) = apply {
    if (this is ViewState.Loading) block()
}

inline fun ViewState.doOnSuccess(crossinline block: (Int) -> Unit) = apply {
    if (this is ViewState.Success) block(type)
}

inline fun ViewState.doOnError(crossinline block: (Int, String?) -> Unit) = apply {
    if (this is ViewState.Error) block(type, msg)
}

suspend inline fun ViewState.doOnLoadingInCoroutine (crossinline block: suspend () -> Unit) = apply {
    if (this is ViewState.Loading) block()
}

suspend inline fun ViewState.doOnSuccessInCoroutine (crossinline block: suspend (Int) -> Unit) = apply {
    if (this is ViewState.Success) block(type)
}

suspend inline fun ViewState.doOnErrorInCoroutine (crossinline block: suspend (Int, String?) -> Unit) = apply {
    if (this is ViewState.Error) block(type, msg)
}