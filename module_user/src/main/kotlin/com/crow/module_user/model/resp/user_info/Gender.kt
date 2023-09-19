package com.crow.module_user.model.resp.user_info

import com.squareup.moshi.Json


data class Gender(

    @Json(name =  "key")
    val mKey: Int,

    @Json(name =  "value")
    val mValue: String
)