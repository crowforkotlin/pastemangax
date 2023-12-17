package com.crow.module_main.model.entity

data class SettingContentEntity(
    val mID: Int,
    val mResource: Int?,
    val mContent: String,
    val mTitle: String? = null
)
