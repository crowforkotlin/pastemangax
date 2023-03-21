package com.crow.module_user.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.asyncClear
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_user.model.UserIntent
import com.crow.module_user.model.resp.user_login.LoginResultErrorResp
import com.crow.module_user.model.resp.user_login.LoginResultsOkResp
import com.crow.module_user.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/java/com/crow/module_user/viewmodel
 * @Time: 2023/3/18 21:16
 * @Author: CrowForKotlin
 * @Description: UserViewModel
 * @formatter:on
 **************************/
class UserViewModel(private val repository: UserRepository) : BaseMviViewModel<UserIntent>() {

    // 使用StateFlow设计成 粘性状态
    private var _userInfo = MutableStateFlow<LoginResultsOkResp?>(null)
    val userInfo: StateFlow<LoginResultsOkResp?> get() = _userInfo

    init {
        // 初始化 用户信息
        viewModelScope.launch {
            _userInfo.emit(DataStoreAgent.DATA_USER.asyncDecode().toTypeEntity<LoginResultsOkResp>() ?: return@launch)
        }
    }

    override fun dispatcher(intent: UserIntent) {
        when (intent) {
            is UserIntent.Login -> doLogin(intent)
        }
    }

    private fun doLogin(intent: UserIntent.Login) {
        // 200代表 登录 请求成功
        intent.flowResult(repository.login(intent.username, intent.password)) { value ->
            if (value.mCode == 200) intent.copy(loginResultsOkResp = (toTypeEntity<LoginResultsOkResp>(value.mResults) ?: return@flowResult intent).also { _userInfo.emit(it) })
            else intent.copy(loginResultErrorResp = (toTypeEntity<LoginResultErrorResp>(value.mResults) ?: return@flowResult intent))
        }
    }

    // 清除用户信息
    fun onClearUserInfo() {
        viewModelScope.launch {
            DataStoreAgent.DATA_USER.asyncClear()
            _userInfo.emit(null)
        }
    }
}