package com.crow.module_book.model.resp.comic_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Theme(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)