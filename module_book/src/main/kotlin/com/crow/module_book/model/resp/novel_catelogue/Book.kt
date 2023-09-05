package com.crow.module_book.model.resp.novel_catelogue

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Book(

    @SerialName(value = "name")
    val name: String,

    @SerialName(value = "path_word")
    val pathWord: String,

    @SerialName(value = "uuid")
    val uuid: String
)