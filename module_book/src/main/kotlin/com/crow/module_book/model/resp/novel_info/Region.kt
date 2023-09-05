package com.crow.module_book.model.resp.novel_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Region(
    @SerialName(value = "display")
    val display: String,
    @SerialName(value = "value")
    val value: Int
)