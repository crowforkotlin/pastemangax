package com.crow.module_user.model.resp


import com.crow.module_user.model.resp.user_info.Gender
import com.crow.module_user.model.resp.user_info.Info
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User update info resp
 *
 * @property mGenders 性别集
 * @property mInfo 用户信息
 * @constructor Create empty User update info resp
 */

@Serializable
data class UserUpdateInfoResp(

    @SerialName(value = "genders")
    val mGenders: List<Gender>,

    @SerialName(value = "info")
    val mInfo: Info
)