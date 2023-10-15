package com.crow.module_anime.model.entity

import com.squareup.moshi.Json

data class AccountEntity(

    @Json(name = "username")
    val mUsername: String,

    @Json(name = "password")
    val mPassword: String,

    @Json(name = "token")
    val mToken: String?
)
