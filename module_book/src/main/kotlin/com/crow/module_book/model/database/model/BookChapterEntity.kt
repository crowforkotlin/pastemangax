package com.crow.module_book.model.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "book_chapter")

data class BookChapterEntity(
    @PrimaryKey
    @ColumnInfo(name = "book_uuid") val mBookUuid: String,
    @ColumnInfo(name = "book_name") val mBookName: String,
    @ColumnInfo(name = "book_type") val mChapterType: Int,
    @ColumnInfo(name = "chapter_name") val mChapterName: String,
    @ColumnInfo(name = "chapter_uuid") val mChapterCurrentUuid: String,
    @ColumnInfo(name = "chapter_next_uuid") val mChapterNextUuid: String?,
    @ColumnInfo(name = "chapter_prev_uuid") val mChapterPrevUuid: String?,
)