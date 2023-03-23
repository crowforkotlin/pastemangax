package com.crow.module_user.model.resp

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/model/resp/user_login
 * @Time: 2023/3/19 18:14
 * @Author: CrowForKotlin
 * @Description: LoginResultError
 * @formatter:on
 **************************/

@JsonClass(generateAdapter = true)
data class LoginResultErrorResp(

    @Json(name = "detail")
    val mDetail: String,
)