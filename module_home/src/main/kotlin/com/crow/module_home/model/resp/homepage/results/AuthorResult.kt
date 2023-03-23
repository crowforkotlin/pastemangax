package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

@Serializable
data class AuthorResult(
    @Json(name = "name")
    val name: String,

    @Json(name = "path_word")
    val pathWord: String
)