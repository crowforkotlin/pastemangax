package com.crow.module_home.model.resp.homepage.results

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Pathword result
 *
 * @property mPathWord 路径词
 * @constructor Create empty Pathword result
 */

@Serializable
data class PathwordResult(

    @SerialName(value = "path_word")
    val mPathWord: String
)