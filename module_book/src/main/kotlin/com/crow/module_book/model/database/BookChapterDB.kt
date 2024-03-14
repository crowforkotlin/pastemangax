package com.crow.module_book.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crow.module_book.model.database.dao.BookChapterDao
import com.crow.module_book.model.database.model.BookChapterEntity

@Database(entities = [BookChapterEntity::class], version = 5)
abstract class BookChapterDB : RoomDatabase() {
    abstract fun bookChapterDao(): BookChapterDao
}