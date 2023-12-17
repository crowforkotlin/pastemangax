package com.crow.base.tools.extensions

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

fun copyStream(input: InputStream, output: OutputStream) {
    //文件存储
    val BUFFER_SIZE = 1024 * 2
    val buffer = ByteArray(BUFFER_SIZE)
    val bis = BufferedInputStream(input, BUFFER_SIZE)
    val out = BufferedOutputStream(output, BUFFER_SIZE)
    var count = 0
    var n = 0
    try {
        while (bis.read(buffer, 0, BUFFER_SIZE).also { n = it } != -1) {
            out.write(buffer, 0, n)
            count += n
        }
        out.flush()
        out.close()
        bis.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

val Context.cacheImageDir: File
    get() = File(cacheDir, "shared_image")

/**
 * Returns the uri of a file
 *
 * @param context context of application
 */
fun File.getUriCompat(context: Context): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(context, "com.crow.manga" + ".provider", this)
    } else {
        this.toUri()
    }
}

/**
 * ● 拷贝文件到本地磁盘
 *
 * ● 2023-12-14 01:28:47 周四 上午
 * @author crowforkotlin
 */
fun Context.copyFolder(folderName: String) {

    // 获取文件夹在磁盘上的路径
    val fileFolderOnDisk = File(filesDir, folderName)

    // 获取文件列表
    val files = runCatching { assets.list(folderName) }
        .onFailure { Log.e("tag", "Failed to get asset file list.", it) }
        .getOrNull()

    // 如果文件列表不为空，则遍历文件列表
    files?.forEach { filename ->
        runCatching {
            // 打开输入流
            assets.open("$folderName/$filename").use { inputStream ->
                // 如果文件夹不存在，则创建文件夹
                if (!fileFolderOnDisk.exists()) fileFolderOnDisk.mkdirs()

                // 创建输出文件
                val outFile = File(fileFolderOnDisk, filename).apply {
                    // 如果文件不存在，则创建文件
                    if (!exists()) createNewFile()
                }

                // 打开输出流并复制文件
                FileOutputStream(outFile).use { outputStream ->
                    copyFile(inputStream, outputStream)
                }
            }
        }.onFailure { Log.e("tag", "Failed to copy asset file: $filename", it) }
    }
}

@Throws(IOException::class)
private fun copyFile(ins: InputStream, out: OutputStream) {
    val buffer = ByteArray(1024)
    var read: Int
    while (ins.read(buffer).also { read = it } != -1) {
        out.write(buffer, 0, read)
    }
}