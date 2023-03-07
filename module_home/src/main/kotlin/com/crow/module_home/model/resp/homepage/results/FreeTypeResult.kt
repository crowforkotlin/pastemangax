package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json

/**
 * Free type result
 *
 * @property mDisplay “免费” “付费”
 * @property mValue Int值
 * @constructor Create empty Free type result
 */
data class FreeTypeResult(

    @Json(name = "display")
    val mDisplay: String,

    @Json(name = "value")
    val mValue: Int,
)

