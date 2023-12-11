package com.crow.module_book.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity

data class BookChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "book_name") val mBookName: String,
    @ColumnInfo(name = "book_type") val mChapterType: Int,
    @ColumnInfo(name = "chapter_name") val mChapterName: String,
    @ColumnInfo(name="chapter_uuid") val mChapterUUID: String,
    @ColumnInfo(name="chapter_next_uuid") val mChapterNextUUID: String?,
    @ColumnInfo(name="chapter_prev_uuid") val mChapterPrevUUID: String?,
)