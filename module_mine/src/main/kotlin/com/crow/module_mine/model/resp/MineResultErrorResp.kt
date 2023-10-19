package com.crow.module_mine.model.resp

import com.squareup.moshi.Json

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/model/resp/user_login
 * @Time: 2023/3/19 18:14
 * @Author: CrowForKotlin
 * @Description: LoginResultError
 * @formatter:on
 **************************/

data class MineResultErrorResp(

    @Json(name =  "detail")
    val mDetail: String,
)