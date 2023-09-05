package com.crow.module_main.model.resp.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Url(

    @SerialName(value = "url_link")
    val mLink: String,

    @SerialName(value = "url_name")
    val mName: String
)