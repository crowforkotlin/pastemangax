package com.crow.base.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crow.base.extensions.logError
import com.crow.base.extensions.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

/*************************
 * @ProjectName: JetpackApp
 * @Dir_Path: lib_base/src/main/java/cn/barry/base/viewmodel
 * @Time: 2022/4/26 9:37
 * @Author: BarryAllen
 * @Description: ViewModel 父类
 * @formatter:off
 **************************/
open class BaseViewModel : ViewModel(), IBaseViewModel {

    private var _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> get() = _viewState

    /* 带有失败结果的流启动方式 */
    fun <T> flowLaunch(flow: Flow<T>, successEvent: IBaseVMEvent.OnSuccess<T>, failureEvent: IBaseVMEvent.OnFailure<Throwable>) = viewModelScope.launch {
        try {
            flow
                .onStart { _viewState.setState(ViewState.Loading) }
                .onCompletion { cause -> if (cause == null) _viewState.setState(ViewState.Success()) }
                .catch {
                    _viewState.setState(ViewState.Error(msg = it.localizedMessage))
                    failureEvent.onFailure(it)
                }
                .flowOn(Dispatchers.IO)
                .collect { successEvent.onSuccess(it) }
        } catch (e: Exception) {
            _viewState.setState(ViewState.Error(msg = e.localizedMessage))
            failureEvent.onFailure(e)
            "[flowLaunch] : $e".logError()
        }
    }

    /* 只有成功结果的流启动方式 */
    fun <T> flowLaunch(flow: Flow<T>, successEvent: IBaseVMEvent.OnSuccess<T>) = viewModelScope.launch {
        try {
            flow
                .onStart { _viewState.setState(ViewState.Loading) }
                .onCompletion { cause -> if (cause == null) _viewState.setState(ViewState.Success()) }
                .catch { _viewState.setState(ViewState.Error(msg = it.localizedMessage)) }
                .flowOn(Dispatchers.IO)
                .collect { successEvent.onSuccess(it) }
        } catch (e: Exception) {
            _viewState.setState(ViewState.Error(msg = e.localizedMessage))
            "[flowLaunch] : $e".logError()
        }
    }
}