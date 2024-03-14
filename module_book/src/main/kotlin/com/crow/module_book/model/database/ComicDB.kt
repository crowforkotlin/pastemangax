package com.crow.module_book.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crow.base.component.room.BaseRoomDateConverter
import com.crow.module_book.model.database.dao.ComicDao
import com.crow.module_book.model.database.model.MineReaderComicEntity
import com.crow.module_book.model.database.model.MineReaderSettingEntity

@Database(entities = [MineReaderComicEntity::class, MineReaderSettingEntity::class], version = 5)
@TypeConverters(BaseRoomDateConverter::class)
abstract class ComicDB : RoomDatabase() {

    abstract fun comicDao() : ComicDao
}