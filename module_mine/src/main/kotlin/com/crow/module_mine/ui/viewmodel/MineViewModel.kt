package com.crow.module_mine.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.viewModelScope
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.crow.base.app.app
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.asyncClear
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.dp2px
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.getImageUrl
import com.crow.module_mine.model.MineIntent
import com.crow.module_mine.model.resp.MineLoginResultsOkResp
import com.crow.module_mine.model.resp.MineResultErrorResp
import com.crow.module_mine.network.MineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import com.crow.mangax.R as mangaR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/java/com/crow/module_user/viewmodel
 * @Time: 2023/3/18 21:16
 * @Author: CrowForKotlin
 * @Description: UserViewModel
 * @formatter:on
 **************************/
class MineViewModel(private val repository: MineRepository) : BaseMviViewModel<MineIntent>() {

    // 使用StateFlow设计成 粘性状态
    private var _userInfo = MutableStateFlow<MineLoginResultsOkResp?>(null)
    val userInfo: StateFlow<MineLoginResultsOkResp?> get() = _userInfo

    // 头像链接
    var mIconUrl: String? = null
        private set


    init {
        // 初始化 用户信息
        viewModelScope.launch(Dispatchers.IO) { _userInfo.emit((toTypeEntity<MineLoginResultsOkResp>(DataStoreAgent.DATA_USER.asyncDecode())).also { mIconUrl = it?.mIconUrl }) }
    }

    override fun dispatcher(intent: MineIntent) {
        when (intent) {
            is MineIntent.Login -> doLogin(intent)
            is MineIntent.Reg -> doReg(intent)
            is MineIntent.GetMineUpdateInfo -> doGetUserInfo(intent)
            is MineIntent.GetMineInfo -> { }
        }
    }

    private fun doLogin(intent: MineIntent.Login) {
        // 200代表 登录 请求成功
        flowResult(intent, repository.login(intent.username, intent.password)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) {
                intent.copy(mineLoginResultsOkResp = toTypeEntity<MineLoginResultsOkResp>(value.mResults)?.also {
                    mIconUrl = it.mIconUrl
                    _userInfo.emit(it)
                })
            }
            else {
                intent.copy(mineResultErrorResp = (toTypeEntity<MineResultErrorResp>(value.mResults) ?: return@flowResult intent))
            }
        }
    }

    private fun doReg(intent: MineIntent.Reg) {
        flowResult(intent, repository.reg(intent.username, intent.password)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) intent.copy(mineResultsOkResp = (toTypeEntity(value.mResults) ?: return@flowResult intent))
            else intent.copy(mineResultErrorResp = (toTypeEntity(value.mResults) ?: return@flowResult intent))
        }
    }


    private fun doGetUserInfo(intent: MineIntent.GetMineUpdateInfo) {
        flowResult(intent, repository.getUserUpdateInfo()) { value ->
            intent.copy(mineUpdateInfoResp = value.mResults)
        }
    }

    // 清除用户信息
    fun doClearUserInfo() {
        viewModelScope.launch {
            DataStoreAgent.DATA_USER.asyncClear()
            MangaXAccountConfig.mAccountToken = ""
            mIconUrl = null
            _userInfo.emit(null)
        }
    }

    // 长度不小于6且不包含空
    fun getUsername(text: String): String? = text.run { if (length < 6 || contains(" ")) return null else this }

    fun getPassword(text: String): String? = text.run { if (length < 6 || contains(" ")) return null else this }


    // 加载Icon --- needApply : 是否需要适配固定大小
    inline fun doLoadIcon(context: Context, needApply: Boolean = true, crossinline doOnReady: (resource: Drawable) -> Unit) {
        if (needApply) {
            app.imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(if (mIconUrl == null) mangaR.drawable.base_icon_app else getImageUrl(BaseStrings.URL.MangaFuna.plus(mIconUrl))) // 加载的图片地址或占位符
                    .allowConversionToBitmap(true)
                    .placeholder(mangaR.drawable.base_icon_app) // 设置占位符
                    .transformations(CircleCropTransformation()) // 应用圆形裁剪
                    .scale(Scale.FIT)
                    .decoderFactory { source, option, _ -> Decoder {
                        val size = context.dp2px(48f).toInt()
                        val bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(source.source.source().inputStream()), size, size, true)
                        DecodeResult(drawable = bitmap.toDrawable(option.context.resources), false) }
                    }
                    .target { doOnReady(it) }
                    .build()
            )
            return
        }
        app.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(if (mIconUrl == null) mangaR.drawable.base_icon_app else getImageUrl(BaseStrings.URL.MangaFuna.plus(mIconUrl))) // 加载的图片地址或占位符
                .placeholder(mangaR.drawable.base_icon_app) // 设置占位符
                .target { doOnReady(it) }
                .decoderFactory { source, option, _ -> Decoder {
                    val size = context.dp2px(48f).toInt()
                    val bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(source.source.source().inputStream()), size, size, true)
                    DecodeResult(drawable = bitmap.toDrawable(option.context.resources), false) }
                }
                .build()
        )
    }
}