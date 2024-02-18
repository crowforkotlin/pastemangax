package com.crow.module_book.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.crow.module_book.model.database.model.BookChapterEntity

@Dao
interface BookChapterDao {
    @Query("SELECT * FROM book_chapter")
    fun getAll(): MutableList<BookChapterEntity>

    @Query("SELECT * FROM book_chapter WHERE book_uuid LIKE :bookUuid AND book_type LIKE :bookType LIMIT 1")
    fun find(bookUuid: String, bookType: Int): BookChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg bookChapterEntity: BookChapterEntity)

    @Delete
    fun delete(bookChapterEntity: BookChapterEntity)

    @Update
    fun update(vararg bookChapterEntity: BookChapterEntity): Int

    @Upsert
    fun upSertChapter(bookChapterEntity: BookChapterEntity)
}