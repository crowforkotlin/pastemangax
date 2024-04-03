package com.crow.module_book.model.resp.comic_info

import com.squareup.moshi.Json


data class QuanCai(

    @Json(name =  "count")
    val mCount: Int,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String,
)