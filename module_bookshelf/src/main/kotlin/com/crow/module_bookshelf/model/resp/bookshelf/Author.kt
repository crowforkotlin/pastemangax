package com.crow.module_bookshelf.model.resp.bookshelf

import com.squareup.moshi.Json


data class Author(

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)