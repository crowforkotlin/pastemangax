package com.crow.module_book.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "book_name") val mBookName: String,
    @ColumnInfo(name = "book_type") val mChapterType: Int,
    @ColumnInfo(name = "chapter_name") val mChapterName: String
)