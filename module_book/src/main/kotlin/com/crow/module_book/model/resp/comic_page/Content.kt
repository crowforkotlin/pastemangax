package com.crow.module_book.model.resp.comic_page

import com.squareup.moshi.Json


data class Content(

    @Json(name =  "url")
    val mImageUrl: String,
)