package com.crow.module_book.model.resp.comic_page

import com.squareup.moshi.Json


data class Chapter(
    @Json(name =  "comic_id")
    val mComicId: String,

    @Json(name =  "comic_path_word")
    val mComicPathWord: String,

    @Json(name =  "contents")
    var mContents: MutableList<Content>,

    @Json(name =  "count")
    val mCount: Int,

    @Json(name =  "datetime_created")
    val mDatetimeCreated: String,

    @Json(name =  "group_id")
    val mGroupId: Any?,

    @Json(name =  "group_path_word")
    val mGroupPathWord: String,

    @Json(name =  "img_type")
    val mImageType: Int,

    @Json(name =  "index")
    val mIndex: Int,

    @Json(name =  "is_long")
    val mIsLong: Boolean,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "news")
    val mNews: String,

    @Json(name =  "next")
    val mNext: String?,

    @Json(name =  "ordered")
    val mOrdered: Int,

    @Json(name =  "prev")
    val mPrev: String?,

    @Json(name =  "size")
    val mSize: Int,

    @Json(name =  "type")
    val mType: Int,

    @Json(name =  "uuid")
    val mUuid: String,

    @Json(name =  "words")
    var mWords: MutableList<Int>
)

internal fun Chapter.getSortedContets() = mWords.zip(mContents).sortedBy { it.first }.map { it.second }.toMutableList()