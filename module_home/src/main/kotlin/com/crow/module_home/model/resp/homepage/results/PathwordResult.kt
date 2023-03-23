package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json

/**
 * Pathword result
 *
 * @property mPathWord 路径词
 * @constructor Create empty Pathword result
 */
data class PathwordResult(

    @Json(name = "path_word")
    val mPathWord: String
)