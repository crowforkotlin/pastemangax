package com.crow.module_anime.model.resp.video


import com.squareup.moshi.Json

data class AnimeVideoResp(

    @Json(name = "cartoon")
    val mCartoon: Cartoon,

    @Json(name = "chapter")
    val mChapter: Chapter,

    @Json(name = "is_lock")
    val mIsLock: Boolean,

    @Json(name = "is_login")
    val mIsLogin: Boolean,

    @Json(name = "is_mobile_bind")
    val mIsMobileBind: Boolean,

    @Json(name = "is_vip")
    val mIsVip: Boolean
)