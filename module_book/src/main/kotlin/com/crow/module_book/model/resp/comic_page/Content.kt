package com.crow.module_book.model.resp.comic_page


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Content(

    @SerialName(value = "url")
    val mImageUrl: String,
)