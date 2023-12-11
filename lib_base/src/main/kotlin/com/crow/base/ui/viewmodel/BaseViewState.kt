package com.crow.base.ui.viewmodel

import androidx.fragment.app.FragmentManager
import com.crow.base.ui.dialog.LoadingAnimDialog

/*
@Machine: RedmiBook Pro 15
@RelativePath: cn\barry\base\viewmodel\BaseViewState.kt
@Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\lib_base\src\main\java\cn\barry\base\viewmodel\BaseViewState.kt
@Author: CrowForKotlin
@Time: 2022/4/26 9:46 周二 上午
@Description:
*/

sealed class BaseViewState {

    // 用于预构建
    data object Default : BaseViewState()

    // 正在加载中
    data object Loading : BaseViewState()

    // 加载成功
    data object Success : BaseViewState()

    // With结果
    data object Result : BaseViewState()

    // 加载失败
    class Error(val code: Int = DEFAULT, val msg: String? = null) : BaseViewState() {
        companion object {
            const val DEFAULT = -1
            const val UNKNOW_HOST = -2
        }
    }


}

fun interface IViewStateCallBack {
    fun callback()
}

fun interface IViewStateErrorCallBack {
    fun callback(code: Int, msg: String?)
}


fun interface IViewStateSuspendCallBack {
    suspend fun callback()
}

fun interface IViewStateErrorSuspendCallBack {
    suspend fun callback(code: Int, msg: String?)
}

//自定义error 可以抛出来结束流的运行
class ViewStateException(msg: String, throwable: Throwable? = null) : Exception(msg, throwable)

inline fun BaseViewState.doOnResultWithLoading (fragmentManager: FragmentManager, crossinline onResult: () -> Unit, crossinline animEnd: () -> Unit) {
    when (this) {
        is BaseViewState.Default -> {}
        is BaseViewState.Loading -> LoadingAnimDialog.show(fragmentManager)
        is BaseViewState.Error -> LoadingAnimDialog.dismiss(fragmentManager) { animEnd() }
        is BaseViewState.Success -> LoadingAnimDialog.dismiss(fragmentManager) { animEnd() }
        is BaseViewState.Result -> onResult()
    }
}

suspend inline fun BaseViewState.doOnLoadingInCoroutine(crossinline block: suspend () -> Unit): BaseViewState {
    if (this is BaseViewState.Loading) block()
    return this
}

suspend inline fun BaseViewState.doOnSuccessInCoroutine(crossinline block: suspend () -> Unit): BaseViewState {
    if (this is BaseViewState.Success) block()
    return this
}

suspend inline fun BaseViewState.doOnErrorInCoroutine(crossinline block: suspend (Int, String?) -> Unit): BaseViewState {
    if (this is BaseViewState.Error) block(code, msg)
    return this
}

suspend inline fun BaseViewState.doOnResultInCoroutine(crossinline block: suspend () -> Unit): BaseViewState {
    if (this is BaseViewState.Result) block()
    return this
}

inline fun BaseViewState.doOnLoadingInline(crossinline block: () -> Unit): BaseViewState {
    if (this is BaseViewState.Loading) block()
    return this
}

inline fun BaseViewState.doOnSuccessInline(crossinline block: () -> Unit): BaseViewState {
    if (this is BaseViewState.Success) block()
    return this
}

inline fun BaseViewState.doOnErrorInline(crossinline block: (Int, String?) -> Unit): BaseViewState {
    if (this is BaseViewState.Error) block(code, msg)
    return this
}

inline fun BaseViewState.doOnResultInline(crossinline block: () -> Unit): BaseViewState {
    if (this is BaseViewState.Result) block()
    return this
}

fun BaseViewState.doOnLoading(iViewStateCallBack: IViewStateCallBack): BaseViewState {
    if (this is BaseViewState.Loading) iViewStateCallBack.callback()
    return this
}

fun BaseViewState.doOnSuccess(iViewStateCallBack: IViewStateCallBack): BaseViewState {
    if (this is BaseViewState.Success) iViewStateCallBack.callback()
    return this
}

fun BaseViewState.doOnResult(iViewStateCallBack: IViewStateCallBack): BaseViewState {
    if (this is BaseViewState.Result) iViewStateCallBack.callback()
    return this
}

fun BaseViewState.doOnError(iViewStateErrorCallBack: IViewStateErrorCallBack): BaseViewState {
    if (this is BaseViewState.Error) iViewStateErrorCallBack.callback(code, msg)
    return this
}


suspend fun BaseViewState.doOnLoadingSuspend(iViewStateCallBack: IViewStateSuspendCallBack): BaseViewState {
    if (this is BaseViewState.Loading) iViewStateCallBack.callback()
    return this
}

suspend fun BaseViewState.doOnSuccessSuspend(iViewStateCallBack: IViewStateSuspendCallBack): BaseViewState {
    if (this is BaseViewState.Success) iViewStateCallBack.callback()
    return this
}

suspend fun BaseViewState.doOnResultSuspend(iViewStateCallBack: IViewStateSuspendCallBack): BaseViewState {
    if (this is BaseViewState.Result) iViewStateCallBack.callback()
    return this
}

suspend fun BaseViewState.doOnErrorSuspend(iViewStateErrorCallBack: IViewStateErrorSuspendCallBack): BaseViewState {
    if (this is BaseViewState.Error) iViewStateErrorCallBack.callback(code, msg)
    return this
}