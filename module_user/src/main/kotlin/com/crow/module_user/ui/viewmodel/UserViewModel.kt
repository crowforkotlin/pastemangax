package com.crow.module_user.ui.viewmodel

import android.content.Context
import android.graphics.drawable.Drawable
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
import com.crow.module_user.model.UserIntent
import com.crow.module_user.model.resp.LoginResultsOkResp
import com.crow.module_user.model.resp.UserResultErrorResp
import com.crow.module_user.network.UserRepository
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
class UserViewModel(private val repository: UserRepository) : BaseMviViewModel<UserIntent>() {

    // 使用StateFlow设计成 粘性状态
    private var _userInfo = MutableStateFlow<LoginResultsOkResp?>(null)
    val userInfo: StateFlow<LoginResultsOkResp?> get() = _userInfo

    // 头像链接
    var mIconUrl: String? = null
        private set


    init {
        // 初始化 用户信息
        viewModelScope.launch(Dispatchers.IO) { _userInfo.emit((toTypeEntity<LoginResultsOkResp>(DataStoreAgent.DATA_USER.asyncDecode())).also { mIconUrl = it?.mIconUrl }) }
    }

    override fun dispatcher(intent: UserIntent) {
        when (intent) {
            is UserIntent.Login -> doLogin(intent)
            is UserIntent.Reg -> doReg(intent)
            is UserIntent.GetUserUpdateInfo -> doGetUserInfo(intent)
            is UserIntent.GetUserInfo -> { }
        }
    }

    private fun doLogin(intent: UserIntent.Login) {
        // 200代表 登录 请求成功
        flowResult(intent, repository.login(intent.username, intent.password)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) {
                intent.copy(loginResultsOkResp = toTypeEntity<LoginResultsOkResp>(value.mResults)?.also {
                    mIconUrl = it.mIconUrl
                    _userInfo.emit(it)
                })
            }
            else {
                intent.copy(userResultErrorResp = (toTypeEntity<UserResultErrorResp>(value.mResults) ?: return@flowResult intent))
            }
        }
    }

    private fun doReg(intent: UserIntent.Reg) {
        flowResult(intent, repository.reg(intent.username, intent.password)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) intent.copy(regResultsOkResp = (toTypeEntity(value.mResults) ?: return@flowResult intent))
            else intent.copy(userResultErrorResp = (toTypeEntity(value.mResults) ?: return@flowResult intent))
        }
    }


    private fun doGetUserInfo(intent: UserIntent.GetUserUpdateInfo) {
        flowResult(intent, repository.getUserUpdateInfo()) { value ->
            intent.copy(userUpdateInfoResp = value.mResults)
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
                .load(if (mIconUrl == null) baseR.drawable.base_icon_app else BaseStrings.URL.MangaFuna.plus(mIconUrl))
                .placeholder(baseR.drawable.base_icon_app)
                .apply(RequestOptions().circleCrop().override(context.dp2px(48f).toInt()))
                .into(object : CustomTarget<Drawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { doOnReady(resource) }
                })
            return
        }
        Glide.with(context)
            .load(if (mIconUrl == null) baseR.drawable.base_icon_app else BaseStrings.URL.MangaFuna.plus(mIconUrl))
            .placeholder(baseR.drawable.base_icon_app)
            .into(object : CustomTarget<Drawable>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { doOnReady(resource) }
            })
    }
}