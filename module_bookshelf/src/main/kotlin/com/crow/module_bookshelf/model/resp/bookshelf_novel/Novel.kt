package com.crow.module_bookshelf.model.resp.bookshelf_novel


import com.crow.module_bookshelf.model.resp.bookshelf.Author
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Novel(

    @SerialName(value = "author")
    val mAuthor: List<Author>,

    @SerialName(value = "b_display")
    val mBDisplay: Boolean,

    @SerialName(value = "cover")
    val mCover: String,

    @SerialName(value = "datetime_updated")
    val mDatetimeUpdated: String,

    @SerialName(value = "last_chapter_id")
    val mLastChapterId: String,

    @SerialName(value = "last_chapter_name")
    val mLastChapterName: String,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "status")
    val mStatus: Int,

    @SerialName(value = "theme")
    val mTheme: List<@Polymorphic Any>,

    @SerialName(value = "uuid")
    val mUuid: String
)