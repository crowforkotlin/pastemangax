package com.crow.module_discover.model.resp.novel_tag


import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Theme(
    @SerialName(value = "color_h5")
    val colorH5: @Polymorphic Any?,
    @SerialName(value = "count")
    val count: Int,
    @SerialName(value = "initials")
    val initials: Int,
    @SerialName(value = "logo")
    val logo: @Polymorphic Any?,
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "path_word")
    val pathWord: String
)