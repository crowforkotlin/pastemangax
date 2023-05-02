package com.crow.module_main.model.entity

import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MainAppConfigEntity(

    // 第一次初始化
    val mAppFirstInit: Boolean = false,

    // 站点
    val mSite: String = BaseStrings.URL.CopyManga,

    // 路线 "0", "1"
    val mRoute: String = BaseUser.CURRENT_REGION
)
