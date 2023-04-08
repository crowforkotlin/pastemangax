package com.crow.module_book.model.resp


import com.crow.module_book.model.resp.novel_catelogue.Book
import com.crow.module_book.model.resp.novel_catelogue.Volume
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovelCatelogueResp(
    @Json(name = "book")
    val book: Book,
    @Json(name = "is_lock")
    val isLock: Boolean,
    @Json(name = "is_login")
    val isLogin: Boolean,
    @Json(name = "is_mobile_bind")
    val isMobileBind: Boolean,
    @Json(name = "is_vip")
    val isVip: Boolean,
    @Json(name = "volume")
    val volume: Volume
)