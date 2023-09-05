package com.crow.module_user.model.resp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/model/resp/user_login
 * @Time: 2023/3/19 18:14
 * @Author: CrowForKotlin
 * @Description: LoginResultError
 * @formatter:on
 **************************/

@Serializable
data class UserResultErrorResp(

    @SerialName(value = "detail")
    val mDetail: String,
)