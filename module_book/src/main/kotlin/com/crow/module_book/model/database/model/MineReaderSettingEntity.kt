package com.crow.module_book.model.database.model

import androidx.datastore.preferences.protobuf.Timestamp
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.util.TableInfo
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import java.util.Date

/**
 * ⦁ MineComicInfoEntity
 *
 * ⦁ 2024/2/6 21:04
 * @author crowforkotlin
 * @formatter:on
 */
@Entity(tableName = "reader_setting")
data class MineReaderSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "account") val mAccount: String,
    @ColumnInfo(name = "light") val mLight: Int,
    @ColumnInfo(name = "read_mode") val mReadMode: ComicCategories.Type,
    @ColumnInfo(name = "update_at") val mUpdatedAt: Date,
    @ColumnInfo(name = "created_at") val mCreatedAt: Date
)

