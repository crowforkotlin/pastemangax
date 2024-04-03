package com.crow.module_mine.model.resp

import com.squareup.moshi.Json


data class MineResultsOkResp(

    @Json(name =  "avatar")
    val mAvatar: String?,

    @Json(name =  "datetime_created")
    val mDatetimeCreated: String,

    @Json(name =  "invite_code")
    val mInviteCode: Any?,

    @Json(name =  "nickname")
    val mNickname: String,

    @Json(name =  "token")
    val mToken: Any?,

    @Json(name =  "user_id")
    val mUserId: String,

    @Json(name =  "uuid")
    val mUuid: String
)