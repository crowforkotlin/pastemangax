package com.crow.base.ui.viewmodel.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crow.base.R
import com.crow.base.app.appContext
import com.crow.base.ui.viewmodel.BaseViewState
import com.crow.base.ui.viewmodel.BaseViewState.Error
import com.crow.base.ui.viewmodel.BaseViewState.Loading
import com.crow.base.ui.viewmodel.BaseViewState.Result
import com.crow.base.ui.viewmodel.BaseViewState.Success
import com.crow.base.ui.viewmodel.ViewStateException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
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

    @PublishedApi internal val _sharedFlow: MutableSharedFlow<I> = MutableSharedFlow (2, 4, BufferOverflow.SUSPEND)

    val sharedFlow: SharedFlow<I> get() = _sharedFlow

    open fun dispatcher(intent: I) { }

    open fun dispatcher(intent: I, onEndAction: Runnable) { }

    fun input(intent: I) = dispatcher(intent)

    fun input(intent: I, onEndAction: Runnable) = dispatcher(intent, onEndAction)

    suspend fun output(baseMviSuspendResult: BaseMviSuspendResult<I>) {
        _sharedFlow.collect { baseMviSuspendResult.onResult(it) }
    }

    // 将 Flow<T> 转换成适合于 MVI 架构的结果，并利用 _sharedFlow.emit() 发送结果到 UI。
    fun <T> flowResult(intent: I, flow: Flow<T>, result: BaseMviFlowResult<I, T>) {
        viewModelScope.launch {
            flow
                .onStart { emitValueMoreoverDelayAfter(intent.also { it.mBaseViewState = Loading }) }
                .onCompletion { emitValueMoreoverDelayAfter(intent.also { it.mBaseViewState = Success }) }
                .catch { catch -> emitValueMoreoverDelayAfter(intent.also { it.mBaseViewState = Error(if (catch is ViewStateException) Error.UNKNOW_HOST else Error.DEFAULT, msg = catch.message ?: appContext.getString(R.string.BaseUnknowError)) })}
                .collect { emitValueMoreoverDelayAfter(result.onResult(it).also { event -> event.mBaseViewState = Result }) }
        }
    }

    private suspend  fun emitValueMoreoverDelayAfter(result: I, delayMs: Long = 1L) {
        _sharedFlow.emit(result)
        delay(delayMs)
    }


    // 将 Flow<T> 转换成适合于 MVI 架构的结果，并根据 意图判断是否需要通过 _sharedFlow.emit() 发送结果到 UI 否则 直接获取结果。
    suspend fun <T> flowResult(flow: Flow<T>, intent: I? = null, context: CoroutineContext = Dispatchers.Main, result: BaseMviFlowResult<I, T>) = suspendCancellableCoroutine { continuation ->
        viewModelScope.launch(context) {
            flow
                .onStart { trySendIntent(intent, Loading) }
                .onCompletion { catch -> trySendIntent(intent, Success) { if (catch != null && !continuation.isCompleted) continuation.resumeWithException(catch) } }
                .catch { catch ->
                    trySendIntent(intent, Error (if (catch is ViewStateException) Error.UNKNOW_HOST else Error.DEFAULT , catch.message ?: appContext.getString(R.string.BaseUnknowError))) {
                        if (!continuation.isCompleted) continuation.resumeWithException(catch)
                    }
                }
                .collect {
                    if (intent != null) emitValueMoreoverDelayAfter(result.onResult(it).also { event -> event.mBaseViewState = Result })
                    if (!continuation.isCompleted) continuation.resume(it)
                }
        }
    }

    private suspend inline fun trySendIntent(intent: I?, state: BaseViewState, endLogic: () -> Unit = {}): I? {
        if (intent != null) {
            intent.mBaseViewState = state
            emitValueMoreoverDelayAfter(intent)
        }
        endLogic()
        return intent
    }

    inline fun toEmitValue(context: CoroutineContext = Dispatchers.Main, crossinline result: suspend () -> I) {
        viewModelScope.launch(context) { _sharedFlow.emit(result().also { it.mBaseViewState = Result }) }
    }
}