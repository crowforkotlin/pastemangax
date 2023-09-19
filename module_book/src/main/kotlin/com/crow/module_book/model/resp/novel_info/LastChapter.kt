package com.crow.module_book.model.resp.novel_info

import com.squareup.moshi.Json


data class LastChapter(
    @Json(name =  "id")
    val mId: String,

    @Json(name =  "name")
    val mName: String
)