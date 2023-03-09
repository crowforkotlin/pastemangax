package com.crow.base.network.filework

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/ssti/laser/app/utils
 * @Time: 2022/11/10 16:05
 * @Author: CrowForKotlin
 * @Description: FileUtils
 * @formatter:on
 **************************/

object FileUtils {

    const val CHUNK_LENGTH: Int = 1024 * 1024

    /**
     * 获取分块字节
     *
     * @param offset
     * @param file
     * @param blockSize
     * @return
     */
    fun getBlock(offset: Long, file: File?, blockSize: Int = CHUNK_LENGTH.toInt()): ByteArray? {
        val bufferSize = ByteArray(blockSize)               // 缓冲区1M
        var accessFile: RandomAccessFile? = null
        try {
            accessFile = RandomAccessFile(file, "r")  // Read Only
            accessFile.seek(offset)
            return when (val readLength = accessFile.read(bufferSize)) {
                -1 -> {
                    null
                }
                blockSize -> {
                    bufferSize
                }
                else -> {
                    val bufferArray = ByteArray(readLength)
                    System.arraycopy(bufferSize, 0, bufferArray, 0, readLength)
                    bufferArray
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            accessFile?.close()
        }
        return null
    }

}