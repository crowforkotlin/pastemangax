package com.crow.module_comic.model.resp.comic


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Results(
    @Json(name = "chapter")
    val chapter: Chapter,

    @Json(name = "comic")
    val comic: Comic,

    @Json(name = "is_lock")
    val isLock: Boolean,

    @Json(name = "is_login")
    val isLogin: Boolean,

    @Json(name = "is_mobile_bind")
    val isMobileBind: Boolean,

    @Json(name = "is_vip")
    val isVip: Boolean,

    @Json(name = "show_app")
    val showApp: Boolean
)