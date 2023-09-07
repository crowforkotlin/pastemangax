package com.crow.module_discover.model.resp.comic_tag

import com.squareup.moshi.Json


data class Theme(

    @Json(name =  "color_h5")
    val mColorH5: Any?,

    @Json(name =  "count")
    val mCount: Int,

    @Json(name =  "initials")
    val mInitials: Int,

    @Json(name =  "logo")
    val mLogo: Any?,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String
)