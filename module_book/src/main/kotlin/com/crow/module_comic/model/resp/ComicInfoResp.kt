package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.comic_info.ComicInfoResult
import com.crow.module_comic.model.resp.comic_info.Groups
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Results
 *
 * @property mComicInfoResult 漫画集
 * @property mGroups
 * @property mIsLock
 * @property mIsLogin
 * @property mIsMobileBind
 * @property mIsVip
 * @property mPopular
 * @constructor Create empty Results
 */
@JsonClass(generateAdapter = true)
data class ComicInfoResp(
    @Json(name = "comic")
    val mComic: ComicInfoResult?,

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