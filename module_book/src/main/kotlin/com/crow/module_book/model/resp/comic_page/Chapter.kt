package com.crow.module_book.model.resp.comic_page

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    @SerialName(value = "comic_id")
    val mComicId: String,

    @SerialName(value = "comic_path_word")
    val mComicPathWord: String,

    @SerialName(value = "contents")
    var mContents: MutableList<Content>,

    @SerialName(value = "count")
    val mCount: Int,

    @SerialName(value = "datetime_created")
    val mDatetimeCreated: String,

    @SerialName(value = "group_id")
    val mGroupId: @Polymorphic Any?,

    @SerialName(value = "group_path_word")
    val mGroupPathWord: String,

    @SerialName(value = "img_type")
    val mImageType: Int,

    @SerialName(value = "index")
    val mIndex: Int,

    @SerialName(value = "is_long")
    val mIsLong: Boolean,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "news")
    val mNews: String,

    @SerialName(value = "next")
    val mNext: String?,

    @SerialName(value = "ordered")
    val mOrdered: Int,

    @SerialName(value = "prev")
    val mPrev: String?,

    @SerialName(value = "size")
    val mSize: Int,

    @SerialName(value = "type")
    val mType: Int,

    @SerialName(value = "uuid")
    val mUuid: String,

    @SerialName(value = "words")
    val mWords: List<Int>
)

internal fun Chapter.getSortedContets() = mWords.zip(mContents).sortedBy { it.first }.map { it.second }.toMutableList()