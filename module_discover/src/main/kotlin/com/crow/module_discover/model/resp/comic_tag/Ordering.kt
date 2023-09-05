package com.crow.module_discover.model.resp.comic_tag


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ordering(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)