package com.crow.module_comic.model.resp.comic_info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Results
 *
 * @property mComic 漫画集
 * @property mGroups
 * @property mIsLock
 * @property mIsLogin
 * @property mIsMobileBind
 * @property mIsVip
 * @property mPopular
 * @constructor Create empty Results
 */
@JsonClass(generateAdapter = true)
data class Results(
    @Json(name = "comic")
    val mComic: Comic?,

    @Json(name = "groups")
    val mGroups: Groups?,

    @Json(name = "is_lock")
    val mIsLock: Boolean?,

    @Json(name = "is_login")
    val mIsLogin: Boolean?,

    @Json(name = "is_mobile_bind")
    val mIsMobileBind: Boolean?,

    @Json(name = "is_vip")
    val mIsVip: Boolean?,

    @Json(name = "popular")
    val mPopular: Int?
)