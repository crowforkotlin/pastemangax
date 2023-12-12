package com.crow.mangax.tools.language

import android.content.Context
import android.util.Log
import com.crow.base.tools.extensions.log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by zhangqichuan on 29/2/16.
 */
object ChineseConverter {

    init {
        System.loadLibrary("ChineseConverter")
        "LOAD".log()
    }
    
    /***
     * @param text           the text to be converted to
     * @param conversionType the conversion type
     * @param context        android context
     * @return the converted text
     */
    fun convert(text: String, conversionType: ConversionType, context: Context): String {
        val lastDataFile = File(context.filesDir.toString() + "/openccdata/zFinished2")
        if (!lastDataFile.exists()) {
            initialize(context)
        }
        val dataFolder = File(context.filesDir.toString() + "/openccdata")
        return convert(text, conversionType.value, dataFolder.absolutePath)
    }

    /***
     * Clear the dictionary data folder, only call this method when update the dictionary data.
     * @param context
     */
    fun clearDictDataFolder(context: Context) {
        val dataFolder = File(context.filesDir.toString() + "/openccdata")
        deleteRecursive(dataFolder)
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory()) for (child in fileOrDirectory.listFiles()) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    private external fun convert(text: String, configFile: String, absoluteDataFolderPath: String): String

    private fun initialize(context: Context) {
        copyFolder("openccdata", context)
    }

    private fun copyFolder(folderName: String, context: Context) {
        val fileFolderOnDisk = File(context.filesDir.toString() + "/" + folderName)
        val assetManager = context.assets
        var files: Array<String>? = null
        try {
            files = assetManager.list(folderName)
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }
        if (files != null) {
            for (filename in files) {
                var `in`: InputStream? = null
                var out: OutputStream? = null
                try {
                    `in` = assetManager.open("$folderName/$filename")
                    if (!fileFolderOnDisk.exists()) {
                        fileFolderOnDisk.mkdirs()
                    }
                    val outFile = File(fileFolderOnDisk.absolutePath, filename)
                    if (!outFile.exists()) {
                        outFile.createNewFile()
                    }
                    out = FileOutputStream(outFile)
                    copyFile(`in`, out)
                } catch (e: IOException) {
                    Log.e("tag", "Failed to copy asset file: $filename", e)
                } finally {
                    if (`in` != null) {
                        try {
                            `in`.close()
                        } catch (e: IOException) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close()
                        } catch (e: IOException) {
                            // NOOP
                        }
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }
}