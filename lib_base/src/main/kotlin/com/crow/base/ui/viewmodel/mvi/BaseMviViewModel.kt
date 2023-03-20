package com.crow.base.ui.viewmodel.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crow.base.tools.extensions.logMsg
import com.crow.base.ui.viewmodel.ViewState
import com.crow.base.ui.viewmodel.ViewStateException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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


    fun <T> I.flowResult(flow: Flow<T>, context: CoroutineContext = Dispatchers.Main, result: BaseMviFlowResult<I, T>) {
        viewModelScope.launch(context) {
            flow
                .onStart {
                    "(MviViewModel) onStart".logMsg()
                    this@flowResult.mViewState = ViewState.Loading
                    _sharedFlow.emit(this@flowResult)
                }
                .onCompletion {
                    "(MviViewModel) onCompletion".logMsg()
                    this@flowResult.mViewState = ViewState.Success
                    _sharedFlow.emit(this@flowResult)
                }
                .catch { catch ->
                    "(MviViewModel) Catch".logMsg()
                    var code = ViewState.Error.DEFAULT
                    if (catch is ViewStateException) code = ViewState.Error.UNKNOW_HOST
                    this@flowResult.mViewState = ViewState.Error(code, msg = catch.message ?: "Unknow")
                    _sharedFlow.emit(this@flowResult)
                }
                .collect {
                    "(MviViewModel) collect".logMsg()
                    _sharedFlow.emit(result.onResult(it).also { event -> event.mViewState = ViewState.Result })
                }
        }
    }
    inline fun toEmitValue(context: CoroutineContext = Dispatchers.Main, crossinline result: suspend () -> I) {
        viewModelScope.launch(context) {
            _sharedFlow.emit(result().also { it.mViewState = ViewState.Result }) }
    }
}