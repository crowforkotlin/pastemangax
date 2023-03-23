package com.crow.module_bookshelf.model.resp.book_shelf


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LastBrowse(

    @Json(name = "last_browse_id")
    val mLastBrowseId: String,

    @Json(name = "last_browse_name")
    val mLastBrowseName: String
)