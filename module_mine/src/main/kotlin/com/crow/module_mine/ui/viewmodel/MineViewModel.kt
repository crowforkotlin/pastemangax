package com.crow.module_mine.ui.viewmodel

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUserConfig
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.asyncClear
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.dp2px
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_mine.model.MineIntent
import com.crow.module_mine.model.resp.MineLoginResultsOkResp
import com.crow.module_mine.model.resp.MineResultErrorResp
import com.crow.module_mine.network.MineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import com.crow.base.R as baseR

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
            BaseUserConfig.CURRENT_USER_TOKEN = ""
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
            Glide.with(context)
                .load(if (mIconUrl == null) ContextCompat.getDrawable(context, baseR.drawable.base_icon_app) else BaseStrings.URL.MangaFuna.plus(mIconUrl))
                .placeholder(baseR.drawable.base_icon_app)
                .apply(RequestOptions().circleCrop().override(context.dp2px(48f).toInt()))
                .into(object : CustomTarget<Drawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { doOnReady(resource) }
                })
            return
        }
        Glide.with(context)
            .load(if (mIconUrl == null) ContextCompat.getDrawable(context, baseR.drawable.base_icon_app) else BaseStrings.URL.MangaFuna.plus(mIconUrl))
            .placeholder(baseR.drawable.base_icon_app)
            .into(object : CustomTarget<Drawable>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { doOnReady(resource) }
            })
    }
}