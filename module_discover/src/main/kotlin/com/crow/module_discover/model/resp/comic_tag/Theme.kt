package com.crow.module_discover.model.resp.comic_tag


import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Theme(

    @SerialName(value = "color_h5")
    val mColorH5: @Polymorphic Any?,

    @SerialName(value = "count")
    val mCount: Int,

    @SerialName(value = "initials")
    val mInitials: Int,

    @SerialName(value = "logo")
    val mLogo: @Polymorphic Any?,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String
)