package com.crow.module_book.model.resp.comic_info

import com.squareup.moshi.Json


data class Groups(

    @Json(name =  "default")
    val mDefault: Default?,

    @Json(name = "karapeji")
    val mQuanCai: QuanCai? = null
)