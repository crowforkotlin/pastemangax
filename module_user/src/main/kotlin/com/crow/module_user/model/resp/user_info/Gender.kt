package com.crow.module_user.model.resp.user_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Gender(

    @SerialName(value = "key")
    val mKey: Int,

    @SerialName(value = "value")
    val mValue: String
)