package com.crow.module_discover.model.resp.novel_tag

import com.squareup.moshi.Json


data class Theme(
    @Json(name =  "color_h5")
    val colorH5: Any?,
    @Json(name =  "count")
    val count: Int,
    @Json(name =  "initials")
    val initials: Int,
    @Json(name =  "logo")
    val logo: Any?,
    @Json(name =  "name")
    val name: String,
    @Json(name =  "path_word")
    val pathWord: String
)