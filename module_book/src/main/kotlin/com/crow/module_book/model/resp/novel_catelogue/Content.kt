package com.crow.module_book.model.resp.novel_catelogue

import com.squareup.moshi.Json


data class Content(
    @Json(name =  "content")
    val content: String?,
    @Json(name =  "content_type")
    val contentType: Int,
    @Json(name =  "end_lines")
    val endLines: Int,
    @Json(name =  "name")
    val name: String,
    @Json(name =  "start_lines")
    val startLines: Int
)