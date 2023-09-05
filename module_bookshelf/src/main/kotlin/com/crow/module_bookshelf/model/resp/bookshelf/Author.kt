package com.crow.module_bookshelf.model.resp.bookshelf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Author(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)