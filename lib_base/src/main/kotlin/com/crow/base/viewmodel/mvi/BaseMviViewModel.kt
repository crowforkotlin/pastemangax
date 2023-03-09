package com.crow.base.viewmodel.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crow.base.extensions.logMsg
import com.crow.base.viewmodel.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
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
abstract class BaseMviViewModel<E : BaseMviEvent> : ViewModel() {

    fun interface BaseMviFlowResult<E : BaseMviEvent, T> { fun onResult(value: T): E }

    @PublishedApi internal val _sharedFlow: MutableSharedFlow<E> = MutableSharedFlow (extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val sharedFlow: SharedFlow<E> get() = _sharedFlow

    open fun dispatcher(event: E) { }

    fun input(event: E) = dispatcher(event)

    inline fun output(crossinline observer: (E) -> Unit) = viewModelScope.launch { _sharedFlow.collect { observer.invoke(it) } }

    fun <T> flowResult(flow: Flow<T>, event: E, context: CoroutineContext = Dispatchers.IO,  result: BaseMviFlowResult<E, T>) {
        viewModelScope.launch {
            flow
                .onStart {
                    currentCoroutineContext().logMsg()
                    _sharedFlow.emit(event.also { it.mViewState = ViewState.Loading }) }
                .onCompletion { _sharedFlow.emit(event.also { it.mViewState = ViewState.Success(ViewState.Success.NO_ATTACH_VALUE) }) }
                .catch { catch -> _sharedFlow.emit(event.also { it.mViewState = ViewState.Error(msg = catch.message ?: "") }) }
                .flowOn(context)
                .collect { _sharedFlow.emit(result.onResult(it).also { event -> event.mViewState = ViewState.Success(ViewState.Success.ATTACH_VALUE) }) }
        }
    }
}