package com.crow.module_book.model.resp.comic_page


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Content(

    @Json(ignore = true)
    var mTips: String? = null,

    @Json(ignore = true)
    var mPrev: String? = null,

    @Json(ignore = true)
    var mNext: String? = null,

    @Json(ignore = true)
    var mIsLoading: Boolean = false,

    @Json(name = "url")
    val mImageUrl: String,
)