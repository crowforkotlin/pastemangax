package com.crow.base.ui.viewmodel.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crow.base.R
import com.crow.base.app.appContext
import com.crow.base.tools.extensions.logMsg
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.ViewState.*
import com.crow.base.ui.viewmodel.ViewStateException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/viewmodel
 * @Time: 2023/3/9 14:40
 * @Author: CrowForKotlin
 * @Description: BaseMviViewModel
 * @formatter:on
 **************************/

abstract class BaseMviViewModel<I : BaseMviIntent> : ViewModel() {

    fun interface BaseMviFlowResult<R : BaseMviIntent, T> { suspend fun onResult(value: T): R }
    fun interface BaseMviSuspendResult<T> { suspend fun onResult(value: T) }

    @PublishedApi internal val _sharedFlow: MutableSharedFlow<I> = MutableSharedFlow (1, 3, BufferOverflow.DROP_OLDEST)

    val sharedFlow: SharedFlow<I> get() = _sharedFlow

    open fun dispatcher(intent: I) { }

    fun input(intent: I) = dispatcher(intent)

    suspend fun output(baseMviSuspendResult: BaseMviSuspendResult<I>) {
        _sharedFlow.collect { baseMviSuspendResult.onResult(it) }
    }

    // 将 Flow<T> 转换成适合于 MVI 架构的结果，并利用 _sharedFlow.emit() 发送结果到 UI。
    fun <T> flowResult(intent: I, flow: Flow<T>, context: CoroutineContext = Dispatchers.Main, result: BaseMviFlowResult<I, T>) {
        viewModelScope.launch(context) {
            flow
                .onStart { _sharedFlow.emit(intent.also { it.mViewState = Loading }) }
                .onCompletion { _sharedFlow.emit(intent.also { it.mViewState = Success }) }
                .catch { catch -> _sharedFlow.emit(intent.also { it.mViewState = Error(if (catch is ViewStateException) Error.UNKNOW_HOST else Error.DEFAULT, msg = catch.message ?: appContext.getString(R.string.BaseUnknow)) }) }
                .collect { _sharedFlow.emit(result.onResult(it).also { event -> event.mViewState = Result }) }
        }
    }

    // 将 Flow<T> 转换成适合于 MVI 架构的结果，并根据 意图判断是否需要通过 _sharedFlow.emit() 发送结果到 UI 否则 直接获取结果。
    suspend fun <T> flowResult(flow: Flow<T>, intent: I? = null, context: CoroutineContext = Dispatchers.Main, result: BaseMviFlowResult<I, T>) = suspendCancellableCoroutine { continuation ->
        viewModelScope.launch(context) {
            flow
                .onStart { trySendIntent(intent, Loading) }
                .onCompletion { catch -> trySendIntent(intent, Success) { if (catch != null && !continuation.isCompleted) continuation.resumeWithException(catch) } }
                .catch { catch ->
                    trySendIntent(intent, Error (if (catch is ViewStateException) Error.UNKNOW_HOST else Error.DEFAULT , catch.message ?: appContext.getString(R.string.BaseUnknow))) {
                        if (!continuation.isCompleted) continuation.resumeWithException(catch)
                    }
                }
                .collect {
                    if (intent != null) _sharedFlow.emit(result.onResult(it).also { event -> event.mViewState = Result })
                    if (!continuation.isCompleted) continuation.resume(it)
                }
        }
    }

    private suspend inline fun<T> T.trySendIntent(intent: I?, state: ViewState, endLogic: () -> Unit = {}): I? {
        if (intent != null) {
            intent.mViewState = state
            _sharedFlow.emit(intent)
        }
        endLogic()
        return intent
    }

    inline fun toEmitValue(context: CoroutineContext = Dispatchers.Main, crossinline result: suspend () -> I) {
        viewModelScope.launch(context) { _sharedFlow.emit(result().also { it.mViewState = ViewState.Result }) }
    }
}