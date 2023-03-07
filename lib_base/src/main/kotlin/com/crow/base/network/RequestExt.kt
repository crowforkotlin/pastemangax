package com.crow.base.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/network
 * @Time: 2022/6/5 18:56
 * @Author: BarryAllen
 * @Description: RequestExt
 * @formatter:on
 **************************/

/* 多文件转成MultiPartBody类型 */
fun filesToMultipartBody(files: List<File>, mediaType: String = "image/jpeg"): MultipartBody? {
    mediaType.toMediaTypeOrNull()?.let { type ->
        MultipartBody.Builder().apply {
            files.forEach { file ->
                addFormDataPart("file", file.name, file.asRequestBody(type))
            }
            setType(MultipartBody.FORM)
            return build()
        }
    }
    return null
}

fun File.asMultipartBodyPart(name: String, mediaType: String = "*/*"): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name,
        this.name,
        this.asRequestBody(mediaType.toMediaType())
    )
}