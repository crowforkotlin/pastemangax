package com.crow.module_user.model.resp.user_info

import com.squareup.moshi.Json


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

data class Info(

    @Json(name =  "avatar")
    val mIconUrl: String,

    @Json(name =  "avatar_rp")
    val mIconUrlRP: String,

    @Json(name =  "gender")
    val mGender: GenderX,

    @Json(name =  "invite_code")
    val mInviteCode: Any?,

    @Json(name =  "mobile")
    val mMobile: Any?,

    @Json(name =  "mobile_region")
    val mMobileRegion: Any?,

    @Json(name =  "nickname")
    val mNickname: String,

    @Json(name =  "username")
    val mUsername: String
)