package com.crow.module_home.model.resp.search.comic_reuslt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Author
 *
 * @property mAlias 作者别名
 * @property mName 作者笔名
 * @property mPathword 关键词
 * @constructor Create empty Author
 */

@Serializable
data class Author(

    @SerialName(value = "alias")
    val mAlias: String?,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathword: String
)