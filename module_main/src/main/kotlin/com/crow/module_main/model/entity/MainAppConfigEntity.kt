package com.crow.module_main.model.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MainAppConfigEntity(

    // 第一次初始化
    val mAppFirstInit: Boolean,

    // 站点
    val mSite: String
)
