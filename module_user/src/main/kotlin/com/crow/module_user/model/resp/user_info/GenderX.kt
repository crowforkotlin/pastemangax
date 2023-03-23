package com.crow.module_user.model.resp.user_info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenderX(

    @Json(name = "display")
    val mDisplay: String,

    @Json(name = "value")
    val mValue: Int
)