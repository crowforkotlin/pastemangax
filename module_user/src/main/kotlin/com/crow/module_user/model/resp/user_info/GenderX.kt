package com.crow.module_user.model.resp.user_info

import com.squareup.moshi.Json


data class GenderX(

    @Json(name =  "display")
    val mDisplay: String,

    @Json(name =  "value")
    val mValue: Int
)