package com.crow.module_anime.model.resp


import com.squareup.moshi.Json

data class UserFailureResp(

    @Json(name =  "detail")
    val mDetail: String,
)