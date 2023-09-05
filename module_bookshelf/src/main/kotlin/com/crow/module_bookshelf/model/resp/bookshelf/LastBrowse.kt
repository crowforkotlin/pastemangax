package com.crow.module_bookshelf.model.resp.bookshelf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LastBrowse(

    @SerialName(value = "last_browse_id")
    val mLastBrowseId: String,

    @SerialName(value = "last_browse_name")
    val mLastBrowseName: String
)