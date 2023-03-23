package com.crow.base.tools.extensions

import java.io.*

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/extensions
 * @Time: 2022/11/16 22:36
 * @Author: CrowForKotlin
 * @Description: FileExt
 * @formatter:on
 **************************/

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