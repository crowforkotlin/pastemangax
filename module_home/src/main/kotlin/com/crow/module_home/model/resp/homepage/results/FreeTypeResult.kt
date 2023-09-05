package com.crow.module_home.model.resp.homepage.results

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Free code result
 *
 * @property mDisplay “免费” “付费”
 * @property mValue Int值
 * @constructor Create empty Free code result
 */

@Serializable
data class FreeTypeResult(

    @SerialName(value = "display")
    val mDisplay: String,

    @SerialName(value = "value")
    val mValue: Int,
)

