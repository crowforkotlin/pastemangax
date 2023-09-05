package com.crow.module_book.model.resp.comic_page

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comic(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "restrict")
    val mRestrict: Restrict,

    @SerialName(value = "uuid")
    val mUuid: String
)