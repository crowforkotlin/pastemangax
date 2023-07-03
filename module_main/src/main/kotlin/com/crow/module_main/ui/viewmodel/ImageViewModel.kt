package com.crow.module_main.ui.viewmodel

import android.graphics.Bitmap
import android.os.Environment
import com.crow.base.ui.viewmodel.BaseViewState
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.io.File
import java.io.FileOutputStream


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.viewmodel
 * @Time: 2023/7/2 21:48
 * @Author: CrowForKotlin
 * @Description: ImageViewModel
 * @formatter:on
 **************************/
class ImageViewModel : BaseMviViewModel<BaseMviIntent>() {

    companion object {
        private const val DCIM_NAME = "CopyMangaX"
    }

    fun saveBitmapToDCIM(bitmap: Bitmap, name: String): Flow<Pair<String?, BaseViewState>> {
        return flow<Pair<String?, BaseViewState>> {
            val file = File(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DCIM_NAME).also { it.mkdirs() }, "${name}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                emit(file.absolutePath to BaseViewState.Result)
            }
        }
            .onStart { emit(null to BaseViewState.Loading) }
            .onCompletion { emit(null to BaseViewState.Success) }
            .catch { cause -> emit(null to BaseViewState.Error(msg = cause.message)) }
            .flowOn(Dispatchers.IO)
    }
}