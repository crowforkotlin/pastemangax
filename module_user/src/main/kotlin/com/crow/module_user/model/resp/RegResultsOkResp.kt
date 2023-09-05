package com.crow.module_user.model.resp

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegResultsOkResp(

    @SerialName(value = "avatar")
    val mAvatar: String,

    @SerialName(value = "datetime_created")
    val mDatetimeCreated: String,

    @SerialName(value = "invite_code")
    val mInviteCode: @Polymorphic Any?,

    @SerialName(value = "nickname")
    val mNickname: String,

    @SerialName(value = "token")
    val mToken: @Polymorphic Any?,

    @SerialName(value = "user_id")
    val mUserId: String,

    @SerialName(value = "uuid")
    val mUuid: String
)