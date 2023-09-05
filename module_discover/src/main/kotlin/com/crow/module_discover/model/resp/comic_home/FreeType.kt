package com.crow.module_discover.model.resp.comic_home


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FreeType(

    @SerialName(value = "display")
    val mDisplay: String,

    @SerialName(value = "value")
    val mValue: Int
)