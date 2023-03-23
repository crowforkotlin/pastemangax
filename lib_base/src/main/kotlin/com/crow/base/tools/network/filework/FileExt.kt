package com.crow.base.tools.network.filework

import java.io.RandomAccessFile

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/network/filework
 * @Time: 2022/11/11 15:24
 * @Author: CrowForKotlin
 * @Description: File Extensions
 * @formatter:on
 **************************/

fun uploadFile(fileEntity: FileEntity): ByteArray {
    val randomAccessFile = RandomAccessFile(fileEntity.file, "r")          // 只读
    val buffer = ByteArray(FileUtils.CHUNK_LENGTH)
    randomAccessFile.seek(fileEntity.currentChunkPos)
    randomAccessFile.read(buffer, 0, buffer.size)
    return buffer
}