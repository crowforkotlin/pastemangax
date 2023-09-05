package com.crow.module_book.model.resp.comic_info


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastChapter(

    @SerialName(value = "name") val mName: String,

    @SerialName(value = "uuid") val mUuid: String,
)