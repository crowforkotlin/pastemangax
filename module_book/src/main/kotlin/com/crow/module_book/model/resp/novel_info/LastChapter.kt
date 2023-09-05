package com.crow.module_book.model.resp.novel_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LastChapter(
    @SerialName(value = "id")
    val mId: String,

    @SerialName(value = "name")
    val mName: String
)