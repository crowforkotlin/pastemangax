package com.crow.module_book.model.resp.novel_catelogue

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Content(
    @SerialName(value = "content")
    val content: String?,
    @SerialName(value = "content_type")
    val contentType: Int,
    @SerialName(value = "end_lines")
    val endLines: Int,
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "start_lines")
    val startLines: Int
)