package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.novel_info.NovelInfoResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovelInfoResp(
    @Json(name = "book")
    val mNovel: NovelInfoResult,
    @Json(name = "is_lock")
    val isLock: Boolean,
    @Json(name = "is_login")
    val isLogin: Boolean,
    @Json(name = "is_mobile_bind")
    val isMobileBind: Boolean,
    @Json(name = "is_vip")
    val isVip: Boolean,
    @Json(name = "popular")
    val popular: Int
)