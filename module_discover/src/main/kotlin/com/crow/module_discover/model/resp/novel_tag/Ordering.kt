package com.crow.module_discover.model.resp.novel_tag


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ordering(
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "path_word")
    val pathWord: String
)