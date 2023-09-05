package com.crow.module_book.model.resp.novel_catelogue


import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Volume(
    @SerialName(value = "book_id")
    val bookId: String,
    @SerialName(value = "book_path_word")
    val bookPathWord: String,
    @SerialName(value = "contents")
    val contents: List<Content>,
    @SerialName(value = "count")
    val count: Int,
    @SerialName(value = "id")
    val id: String,
    @SerialName(value = "index")
    val index: Int,
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "next")
    val next: String,
    @SerialName(value = "prev")
    val prev: @Polymorphic Any?,
    @SerialName(value = "sort")
    val sort: Int,
    @SerialName(value = "txt_addr")
    val txtAddr: String,
    @SerialName(value = "txt_encoding")
    val txtEncoding: String
)