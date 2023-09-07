package com.crow.module_user.model.resp

import com.crow.module_user.model.resp.user_info.Gender
import com.crow.module_user.model.resp.user_info.Info
import com.squareup.moshi.Json


/**
 * User update info resp
 *
 * @property mGenders 性别集
 * @property mInfo 用户信息
 * @constructor Create empty User update info resp
 */

data class UserUpdateInfoResp(

    @Json(name =  "genders")
    val mGenders: List<Gender>,

    @Json(name =  "info")
    val mInfo: Info
)