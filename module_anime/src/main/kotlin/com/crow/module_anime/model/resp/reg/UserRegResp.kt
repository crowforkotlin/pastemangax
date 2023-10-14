package com.crow.module_anime.model.resp.reg


import com.squareup.moshi.Json

data class UserRegResp(

    @Json(name = "avatar")
    val mAvatar: String,

    @Json(name = "cartoon_vip")
    val mCartoonVip: Int,

    @Json(name = "comic_vip")
    val mComicVip: Int,

    @Json(name = "datetime_created")
    val mDatetimeCreated: String?,

    @Json(name = "invite_code")
    val mInviteCode: String,

    @Json(name = "nickname")
    val mNickname: String,

    @Json(name = "token")
    val mToken: String?,

    @Json(name = "user_id")
    val mUserId: String,

    @Json(name = "uuid")
    val mUUID: String
)