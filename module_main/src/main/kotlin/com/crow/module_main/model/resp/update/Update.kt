package com.crow.module_main.model.resp.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Update (

    @SerialName(value = "update_content")
    val mContent: String,

    @SerialName(value = "update_title")
    val mTitle: String,

    @SerialName(value = "update_url")
    val mUrl: List<Url>,

    @SerialName(value = "update_version_code")
    val mVersionCode: Int,

    @SerialName(value = "update_version_name")
    val mVersionName: String,

    @SerialName(value = "update_time")
    val mTime: String
)
