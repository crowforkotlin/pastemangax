package com.crow.module_book.model.resp.novel_info

import com.squareup.moshi.Json


data class Status(

    @Json(name =  "display")
    val mDisplay: String,

    @Json(name =  "value")
    val mValue: Int
)