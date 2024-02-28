package com.crow.module_book.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.crow.module_book.model.database.model.MineReaderComicEntity
import com.crow.module_book.model.database.model.MineReaderSettingEntity

@Dao
interface ComicDao {
    @Query("SELECT * FROM reader_comic")
    fun getAllComic(): MutableList<MineReaderComicEntity>

    @Query("SELECT * FROM reader_comic WHERE account LIKE :account")
    fun getComicsByAccount(account: String): MutableList<MineReaderComicEntity>

    @Query("SELECT * FROM reader_comic WHERE account LIKE :account AND comic_uuid LIKE :comicUuid AND chapter_uuid LIKE :chapterUuid LIMIT 1")
    fun getComic(account: String, comicUuid: String, chapterUuid: String): MineReaderComicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllComic(vararg comics: MineReaderComicEntity)

    @Delete
    fun deleteComic(comics: MineReaderComicEntity)

    @Update
    fun updateComic(vararg comics: MineReaderComicEntity): Int

    @Query("SELECT * FROM reader_setting")
    fun getAllSetting(): MutableList<MineReaderSettingEntity>

    @Query("SELECT * FROM reader_setting WHERE account LIKE :account")
    fun getSettingByAccount(account: String): MutableList<MineReaderSettingEntity>

    @Query("SELECT * FROM reader_setting WHERE account LIKE :account LIMIT 1")
    fun findSetting(account: String): MineReaderSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSetting(vararg setting: MineReaderSettingEntity)

    @Upsert
    fun upSertSetting(setting: MineReaderSettingEntity)

    @Upsert
    fun upSertReaderComic(comics: MineReaderComicEntity)

    @Delete
    fun deleteSetting(setting: MineReaderSettingEntity)

    @Update
    fun updateSetting(vararg setting: MineReaderSettingEntity): Int
}