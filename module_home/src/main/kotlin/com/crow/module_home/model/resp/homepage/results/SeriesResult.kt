package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json

data class SeriesResult(
    @Json(name = "color")
    val color: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "path_word")
    val pathWord: String
)
