package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.comic_info.ComicInfoResult
import com.crow.module_book.model.resp.comic_info.Groups
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class ComicInfoResp(
    @SerialName(value = "comic")
    val mComic: ComicInfoResult?,

    @SerialName(value = "groups")
    val mGroups: Groups?,

    @SerialName(value = "is_lock")
    val mIsLock: Boolean?,

    @SerialName(value = "is_login")
    val mIsLogin: Boolean?,

    @SerialName(value = "is_mobile_bind")
    val mIsMobileBind: Boolean?,

    @SerialName(value = "is_vip")
    val mIsVip: Boolean?,

    @SerialName(value = "popular")
    val mPopular: Int?
)