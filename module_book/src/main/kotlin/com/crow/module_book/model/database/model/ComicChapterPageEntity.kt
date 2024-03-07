package com.crow.module_book.model.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ⦁ ComicChapterPageENtity
 *
 * ⦁ 2024/2/6 21:04
 * @author crowforkotlin
 * @formatter:on
 */
@Entity
data class ComicChapterPageEntity(
    @PrimaryKey(autoGenerate = true) val mID: Long,
    @ColumnInfo(name = "timestamp") val mTimestamp: String,
    @ColumnInfo(name = "comic_name") val mComicName: String,
    @ColumnInfo(name = "pathword") val mPathword: String,
    @ColumnInfo(name = "uuid") val mUUID: String,
    @ColumnInfo(name = "chapter_id") val mChapterID: String,
    @ColumnInfo(name = "chapter_name") val mChapterName: String,
    @ColumnInfo(name = "chapter_count") val mChapterCount: Int,
    @ColumnInfo(name = "chapter_update") val mChapterUpdate: String,
    @ColumnInfo(name = "chapter_next_uuid") val mNextUUID: String?,
    @ColumnInfo(name = "chapter_prev_uuid") val mPrevUUID: String?,
)