package com.crow.module_book.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.crow.module_book.model.entity.BookChapterEntity

@Dao
interface BookChapterDao {
    @Query("SELECT * FROM BookChapterEntity")
    fun getAll(): MutableList<BookChapterEntity>

    @Query("SELECT * FROM BookChapterEntity WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): MutableList<BookChapterEntity>

    @Query("SELECT * FROM BookChapterEntity WHERE book_name LIKE :bookName AND book_type LIKE :bookType LIMIT 1")
    fun find(bookName: String, bookType: Int): BookChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg bookChapterEntity: BookChapterEntity)

    @Delete
    fun delete(bookChapterEntity: BookChapterEntity)

    @Update
    fun update(vararg bookChapterEntity: BookChapterEntity): Int
}