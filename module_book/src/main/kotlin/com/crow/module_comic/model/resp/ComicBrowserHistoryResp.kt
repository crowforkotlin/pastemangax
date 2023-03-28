package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.comic_browser.Browse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ComicBrowserHistoryResp(

    @Json(name = "browse")
    val browse: Browse?,

    @Json(name = "collect")
    val collect: Int?,

    @Json(name = "is_lock")
    val isLock: Boolean,

    @Json(name = "is_login")
    val isLogin: Boolean,

    @Json(name = "is_mobile_bind")
    val isMobileBind: Boolean,

    @Json(name = "is_vip")
    val isVip: Boolean,
)