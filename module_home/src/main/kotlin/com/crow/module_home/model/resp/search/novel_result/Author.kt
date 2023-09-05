package com.crow.module_home.model.resp.search.novel_result

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Author(

    @SerialName(value = "alias")
    val mAlias: String?,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)