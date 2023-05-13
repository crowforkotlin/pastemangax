package com.crow.module_home.model.resp.search.novel_result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Author(

    @Json(name = "alias")
    val mAlias: String?,

    @Json(name = "name")
    val mName: String,

    @Json(name = "path_word")
    val mPathWord: String
)