package com.crow.module_bookshelf.model.resp.book_shelf


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Author(

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)