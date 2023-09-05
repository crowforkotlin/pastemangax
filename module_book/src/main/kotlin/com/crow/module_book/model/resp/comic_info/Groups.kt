package com.crow.module_book.model.resp.comic_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Groups(

    @SerialName(value = "default")
    val mDefault: Default,
)