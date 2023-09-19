package com.crow.module_book.model.resp.novel_info

import com.squareup.moshi.Json


data class Author(

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String
)