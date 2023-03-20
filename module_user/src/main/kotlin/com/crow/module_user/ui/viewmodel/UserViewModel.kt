package com.crow.module_user.ui.viewmodel

import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_user.model.UserIntent
import com.crow.module_user.model.resp.user_login.LoginResultErrorResp
import com.crow.module_user.model.resp.user_login.LoginResultsOkResp
import com.crow.module_user.network.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/java/com/crow/module_user/viewmodel
 * @Time: 2023/3/18 21:16
 * @Author: CrowForKotlin
 * @Description: UserViewModel
 * @formatter:on
 **************************/
class UserViewModel(private val repository: UserRepository) : BaseMviViewModel<UserIntent>() {

    private var _userInfo = MutableSharedFlow<LoginResultsOkResp>()
    val userInfo: SharedFlow<LoginResultsOkResp> get() = _userInfo

    override fun dispatcher(intent: UserIntent) {
        when (intent) {
            is UserIntent.Login -> doOnLogin(intent)
            is UserIntent.GetUserInfo -> doOnLoadUserInfo(intent)
        }
    }

    private fun doOnLoadUserInfo(intent: UserIntent.GetUserInfo) {
        toEmitValue { intent.copy(userInfo = DataStoreAgent.DATA_USER.asyncDecode().toTypeEntity<LoginResultsOkResp>()) }
    }

    private fun doOnLogin(intent: UserIntent.Login) {
        // 200代表 登录 请求成功
        intent.flowResult(repository.login(intent.username, intent.password)) { value ->
            if (value.mCode == 200) intent.copy(loginResultsOkResp = (toTypeEntity<LoginResultsOkResp>(value.mResults) ?: return@flowResult intent).also { _userInfo.emit(it) })
            else intent.copy(loginResultErrorResp = (toTypeEntity<LoginResultErrorResp>(value.mResults) ?: return@flowResult intent))
        }
    }
}