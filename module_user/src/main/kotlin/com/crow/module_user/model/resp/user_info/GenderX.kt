package com.crow.module_user.model.resp.user_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenderX(

    @SerialName(value = "display")
    val mDisplay: String,

    @SerialName(value = "value")
    val mValue: Int
)