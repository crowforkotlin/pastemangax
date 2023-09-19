package com.crow.module_book.model.resp.comic_info

import com.squareup.moshi.Json


data class Status(

    @Json(name =  "display")
    val mDisplay: String,

    @Json(name =  "value")
    val mValue: Int
) {
    companion object {
        const val LOADING = 0
        const val FINISH = 1
    }
}