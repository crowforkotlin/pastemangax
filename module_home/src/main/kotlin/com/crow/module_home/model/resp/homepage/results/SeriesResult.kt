package com.crow.module_home.model.resp.homepage.results

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesResult(
    @SerialName(value = "color")
    val color: String,

    @SerialName(value = "name")
    val name: String,

    @SerialName(value = "path_word")
    val pathWord: String
)
