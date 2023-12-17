package com.crow.module_main.model.entity

import com.google.android.material.materialswitch.MaterialSwitch

data class SettingSwitchEntity(
    val mID: Int,
    val mResource: Int? = null,
    val mContent: String,
    val mEnable: Boolean
)
