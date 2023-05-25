package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json

data class AuthorResult (
    @Json(name = "name")
    val name: String,

    @Json(name = "path_word")
    val pathWord: String
)