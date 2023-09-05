package com.crow.module_book.model.resp.comic_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FreeType(
    @SerialName(value = "display")
    val mDisplay: String,

    @SerialName(value = "value")
    val mValue: Int
)