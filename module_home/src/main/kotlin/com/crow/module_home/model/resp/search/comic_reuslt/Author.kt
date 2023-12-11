package com.crow.module_home.model.resp.search.comic_reuslt

import com.squareup.moshi.Json


/**
 * Author
 *
 * @property mAlias 作者别名
 * @property mName 作者笔名
 * @property mPathword 关键词
 * @constructor Create empty Author
 */

data class Author(

    @Json(name =  "alias")
    val mAlias: String?,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathword: String
)