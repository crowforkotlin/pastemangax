package com.crow.module_discover.model.resp.comic_tag


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Top(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)