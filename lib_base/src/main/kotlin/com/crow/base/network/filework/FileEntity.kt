package com.crow.base.network.filework

import androidx.annotation.IntRange
import java.io.File

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/network/filework
 * @Time: 2022/11/11 15:30
 * @Author: CrowForKotlin
 * @Description: FileEntity
 * @formatter:on
 **************************/
class FileEntity(
    val file: File,

    @IntRange(from = 1L, to = 999L)
    var currentChunkPos: Long
)