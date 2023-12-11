package com.crow.base.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crow.base.tools.extensions.logError
import com.crow.base.tools.extensions.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/*************************
 * @ProjectName: JetpackApp
 * @Dir_Path: lib_base/src/main/java/cn/barry/base/viewmodel
 * @Time: 2022/4/26 9:37
 * @Author: CrowForKotlin
 * @Description: ViewModel 父类
 * @formatter:off
 **************************/
open class BaseViewModel : ViewModel(), IBaseViewModel {

    private var _Base_viewState = MutableLiveData<BaseViewState>()
    val baseViewState: LiveData<BaseViewState> get() = _Base_viewState

    /* 带有失败结果的流启动方式 */
    fun <T> flowLaunch(flow: Flow<T>, successEvent: IBaseVMEvent.OnSuccess<T>, failureEvent: IBaseVMEvent.OnFailure<Throwable>) = viewModelScope.launch {
        try {
            flow
                .onStart { _Base_viewState.setState(BaseViewState.Loading) }
                .onCompletion { cause -> if (cause == null) _Base_viewState.setState(BaseViewState.Success) }
                .catch {
                    _Base_viewState.setState(BaseViewState.Error(msg = it.localizedMessage))
                    failureEvent.onFailure(it)
                }
                .flowOn(Dispatchers.IO)
                .collect { successEvent.onSuccess(it) }
        } catch (e: Exception) {
            _Base_viewState.setState(BaseViewState.Error(msg = e.localizedMessage))
            failureEvent.onFailure(e)
            "[flowLaunch] : $e".logError()
        }
    }

    /* 只有成功结果的流启动方式 */
    fun <T> flowLaunch(flow: Flow<T>, successEvent: IBaseVMEvent.OnSuccess<T>) = viewModelScope.launch {
        try {
            flow
                .onStart { _Base_viewState.setState(BaseViewState.Loading) }
                .onCompletion { cause -> if (cause == null) _Base_viewState.setState(BaseViewState.Success) }
                .catch { _Base_viewState.setState(BaseViewState.Error(msg = it.localizedMessage)) }
                .flowOn(Dispatchers.IO)
                .collect { successEvent.onSuccess(it) }
        } catch (e: Exception) {
            _Base_viewState.setState(BaseViewState.Error(msg = e.localizedMessage))
            "[flowLaunch] : $e".logError()
        }
    }
}