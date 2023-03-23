package com.crow.module_user.model

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_user.model.resp.LoginResultErrorResp
import com.crow.module_user.model.resp.LoginResultsOkResp
import com.crow.module_user.model.resp.UserUpdateInfoResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/model
 * @Time: 2023/3/18 21:17
 * @Author: CrowForKotlin
 * @Description: UserIntent
 * @formatter:on
 **************************/
sealed class UserIntent : BaseMviIntent() {
    data class Login(
        var username: String,
        var password: String,
        val loginResultsOkResp: LoginResultsOkResp? = null,
        val loginResultErrorResp: LoginResultErrorResp? = null,
    ) : UserIntent()

    data class GetUserUpdateInfo(val userUpdateInfoResp: UserUpdateInfoResp? = null): UserIntent()

    data class GetUserInfo(val userInfo: LoginResultsOkResp? = null) : UserIntent()
}