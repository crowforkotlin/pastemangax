package com.crow.module_bookshelf.model.resp.bookshelf

import com.squareup.moshi.Json




data class LastBrowse(

    @Json(name =  "last_browse_id")
    val mLastBrowseId: String,

    @Json(name =  "last_browse_name")
    val mLastBrowseName: String
)