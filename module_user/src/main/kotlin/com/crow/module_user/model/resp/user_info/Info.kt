package com.crow.module_user.model.resp.user_info

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Info
 *
 * @property mIconUrl 完整的头像地址
 * @property mIconUrlRP 头像路径
 * @property mGender 性别集
 * @property mInviteCode 邀请码
 * @property mMobile 手机用户
 * @property mMobileRegion 区域
 * @property mNickname 昵称
 * @property mUsername 用户名
 * @constructor Create empty Info
 */

@Serializable
data class Info(

    @SerialName(value = "avatar")
    val mIconUrl: String,

    @SerialName(value = "avatar_rp")
    val mIconUrlRP: String,

    @SerialName(value = "gender")
    val mGender: GenderX,

    @SerialName(value = "invite_code")
    val mInviteCode: @Polymorphic Any?,

    @SerialName(value = "mobile")
    val mMobile: @Polymorphic Any?,

    @SerialName(value = "mobile_region")
    val mMobileRegion: @Polymorphic Any?,

    @SerialName(value = "nickname")
    val mNickname: String,

    @SerialName(value = "username")
    val mUsername: String
)