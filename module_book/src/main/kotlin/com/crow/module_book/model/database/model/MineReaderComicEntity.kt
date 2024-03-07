package com.crow.module_book.model.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import java.util.Date

/**
 * ⦁ MineComicInfoEntity
 *
 * ⦁ 2024/2/6 21:04
 * @author crowforkotlin
 * @formatter:on
 */
@Entity(
    tableName = "reader_comic", indices = [Index(value = ["comic_uuid"])],
    primaryKeys = ["account", "comic_uuid"]
)
data class MineReaderComicEntity(
    @ColumnInfo(name = "account") val mAccount: String,
    @ColumnInfo(name = "comic_uuid") val mComicUUID: String,
    @ColumnInfo(name = "chapter_uuid") val mChapterUUID: String,
    @ColumnInfo(name = "chapter_id") val mChapterId: Int,
    @ColumnInfo(name = "chapter_position") val mChapterPosition: Int,
    @ColumnInfo(name = "chapter_position_offset") val mChapterPositionOffset: Int,
    @ColumnInfo(name = "update_at") val mUpdatedAt: Date,
    @ColumnInfo(name = "created_at") val mCreatedAt: Date
)