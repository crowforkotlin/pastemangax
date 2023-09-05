package com.crow.module_book.model.resp.comic_page


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Restrict(

    @SerialName(value = "display")
    val display: String,

    @SerialName(value = "value")
    val value: Int
)