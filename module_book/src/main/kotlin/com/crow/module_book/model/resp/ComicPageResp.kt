package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.comic_page.Chapter
import com.crow.module_book.model.resp.comic_page.Comic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class  ComicPageResp(
    @SerialName(value = "chapter")
    val mChapter: Chapter,

    @SerialName(value = "comic")
    val mComic: Comic,

    @SerialName(value = "is_lock")
    val mIsLock: Boolean,

    @SerialName(value = "is_login")
    val mIsLogin: Boolean,

    @SerialName(value = "is_mobile_bind")
    val mIsMobileBind: Boolean,

    @SerialName(value = "is_vip")
    val mIsVip: Boolean,

    @SerialName(value = "show_app")
    val mShowApp: Boolean
)

internal fun ComicPageResp?.requireContentsSize() = this?.mChapter?.mSize ?: 0