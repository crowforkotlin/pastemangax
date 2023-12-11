package com.crow.module_book.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.crow.module_book.model.dao.BookChapterDao
import com.crow.module_book.model.entity.BookChapterEntity

@Database(entities = [BookChapterEntity::class], version = 2)
abstract class BookChapterDB : RoomDatabase() {
    abstract fun bookChapterDao(): BookChapterDao
}