package com.crow.module_anime.model.resp.info


import com.squareup.moshi.Json

data class AnimeInfoResp(

    @Json(name = "cartoon")
    val mCartoon: Cartoon,

    @Json(name = "collect")
    val mCollect: Any?,

    @Json(name = "is_lock")
    val mIsLock: Boolean,

    @Json(name = "is_login")
    val mIsLogin: Boolean,

    @Json(name = "is_mobile_bind")
    val mIsMobileBind: Boolean,

    @Json(name = "is_vip")
    val mIsVip: Boolean,

    @Json(name = "popular")
    val mPopular: Int
)