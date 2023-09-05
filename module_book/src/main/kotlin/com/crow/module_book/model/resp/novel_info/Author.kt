package com.crow.module_book.model.resp.novel_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Author(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)