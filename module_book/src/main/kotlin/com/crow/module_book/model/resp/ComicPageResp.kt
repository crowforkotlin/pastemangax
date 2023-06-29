package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.comic_page.Chapter
import com.crow.module_book.model.resp.comic_page.Comic
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class  ComicPageResp(
    @Json(name = "chapter")
    val mChapter: Chapter,

    @Json(name = "comic")
    val mComic: Comic,

    @Json(name = "is_lock")
    val mIsLock: Boolean,

    @Json(name = "is_login")
    val mIsLogin: Boolean,

    @Json(name = "is_mobile_bind")
    val mIsMobileBind: Boolean,

    @Json(name = "is_vip")
    val mIsVip: Boolean,

    @Json(name = "show_app")
    val mShowApp: Boolean
)