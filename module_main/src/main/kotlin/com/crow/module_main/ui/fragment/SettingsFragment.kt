package com.crow.module_main.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import com.crow.base.tools.extensions.SpNameSpace
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.animateFadeOutInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.mangax.copymanga.entity.CatlogConfig.mApiProxyEnable
import com.crow.mangax.copymanga.entity.CatlogConfig.mChineseConvert
import com.crow.mangax.copymanga.entity.CatlogConfig.mCoverOrinal
import com.crow.mangax.copymanga.entity.CatlogConfig.mHotAccurateDisplay
import com.crow.mangax.copymanga.entity.CatlogConfig.mUpdatePrefix
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentSettingsBinding
import com.crow.module_main.databinding.MainSettingsProxyLayoutBinding
import com.crow.module_main.databinding.MainSettingsResolutionLayoutBinding
import com.crow.module_main.databinding.MainSettingsSiteLayoutBinding
import com.crow.module_main.model.entity.SettingContentEntity
import com.crow.module_main.model.entity.SettingSwitchEntity
import com.crow.module_main.model.entity.SettingTitleEntity
import com.crow.module_main.model.intent.AppIntent
import com.crow.module_main.ui.adapter.SettingsAdapter
import com.crow.module_main.ui.viewmodel.MainViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import com.crow.mangax.R as mangaR

class SettingsFragment : BaseMviFragment<MainFragmentSettingsBinding>() {

    /**
     * ⦁ 屏幕高度 / 6
     *
     * ⦁ 2023-10-02 23:27:58 周一 下午
     */
    private val mScreenHeight by lazy { mContext.resources.displayMetrics.heightPixels / 6 }

    /**
     * ⦁ 容器VM
     *
     * ⦁ 2023-10-02 23:28:07 周一 下午
     */
    private val mVM by viewModel<MainViewModel>()

    /**
     * ⦁ 站点Dialog
     *
     * ⦁ 2023-10-02 23:28:32 周一 下午
     */
    private var mSiteAlertDialog: AlertDialog? = null

    /**
     * ⦁ 站点Dialog Binding
     *
     * ⦁ 2023-10-02 23:28:42 周一 下午
     */
    private var mSiteDialogBinding: MainSettingsSiteLayoutBinding? = null

    /**
     * ⦁ Global BaseEvent
     *
     * ⦁ 2023-10-02 23:41:17 周一 下午
     */
    private val mBaseEvent by lazy { BaseEvent.getSIngleInstance() }

    /**
     * ⦁ Settings Rv Adapter
     *
     * ⦁ 2023-12-15 01:41:54 周五 上午
     * @author crowforkotlin
     */
    private val mAdapter by lazy {
        SettingsAdapter(
            onClick = { onClickItem(it) },
            onChecked = { pos, switch -> onCheckedItem(pos, switch) }
        )
    }

    /**
     * ⦁ 点击Item
     *
     * ⦁ 2023-10-02 23:26:28 周一 下午
     */
    private fun onClickItem(position: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            when(position) {
                0 -> navigateToStyleableFragment()
                1 -> initSiteView()
                2 -> initProxyView()
                3 -> initResolution()
            }
        }
    }

    private fun onCheckedItem(position: Int, switch: MaterialSwitch) {
        when(position) {
            5 -> mVM.saveAppCatLogConfig(SpNameSpace.Key.ENABLE_CHINESE_CONVERT, switch.isChecked)
            6 -> mVM.saveAppCatLogConfig(SpNameSpace.Key.ENABLE_HOT_ACCURATE_DISPLAY, switch.isChecked)
            7 -> mVM.saveAppCatLogConfig(SpNameSpace.Key.ENABLE_UPDATE_PREFIX, switch.isChecked)
            8 -> mVM.saveAppCatLogConfig(SpNameSpace.Key.ENABLE_COVER_ORINAL, switch.isChecked)
        }
        toast(getString(mangaR.string.mangax_restart_effect))
    }



    private suspend fun initResolution() {

        // 获取APP的配置
        val appConfig = mVM.getReadedAppConfig() ?: return run { toast(getString(mangaR.string.mangax_unknow_error)) }

        val binding = MainSettingsResolutionLayoutBinding.inflate(layoutInflater)

        val alertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(binding.root) }

        binding.close.doOnClickInterval { alertDialog.dismiss() }

        when(appConfig.mResolution) {
            800 -> binding.resolution800.isChecked = true
            1200 -> binding.resolution1200.isChecked = true
            1500 -> binding.resolution1500.isChecked = true
        }

        binding.resolutionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            mBaseEvent.doOnInterval {
                when(checkedId) {
                    binding.resolution800.id -> MangaXAccountConfig.mResolution = 800
                    binding.resolution1200.id -> MangaXAccountConfig.mResolution = 1200
                    binding.resolution1500.id -> MangaXAccountConfig.mResolution = 1500
                }
                mVM.saveAppConfig(appConfig.copy(mResolution = MangaXAccountConfig.mResolution))
                mHandler.postDelayed({ alertDialog.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
            }
        }
    }

    private suspend fun initSiteView() {
        // 获取APP的配置
        val appConfig = mVM.getReadedAppConfig() ?: return run { toast(getString(mangaR.string.mangax_unknow_error)) }

        mSiteDialogBinding = MainSettingsSiteLayoutBinding.inflate(layoutInflater)

        mSiteDialogBinding?.apply {
            mSiteAlertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(root) }

            close.doOnClickInterval { mSiteAlertDialog?.dismiss() }

            // 取消弹窗的时候吧 局部AlerDialog和DialogBinding置空 防止泄漏
            mSiteAlertDialog?.setOnDismissListener {
                mSiteDialogBinding = null
                mSiteAlertDialog = null
            }

            // TV后缀设置当前站点为...
            if (appConfig.mCopyMangaSite.endsWith(BaseStrings.URL.CopyManga_TLD_TV)) {
                siteRadioOne.isChecked = true
                siteCurrent.text = getString(R.string.main_site_current, siteRadioOne.text)
            }

            // SITE后缀设置当前站点为...
            else if (appConfig.mCopyMangaSite.endsWith(BaseStrings.URL.CopyManga_TLD_SITE)) {
                siteRadioTwo.isChecked = true
                siteCurrent.text = getString(R.string.main_site_current, siteRadioTwo.text)
            }

            // 否则 当前站点位置
            else { siteCurrent.text = getString(R.string.main_site_current, getString(mangaR.string.mangax_unknow)) }

            // 设置 静态、动态站点的ScrollView最大高度为 屏幕高度像素 / 6
            (siteStaticScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = mScreenHeight
            (siteDynamicScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = mScreenHeight

            // 动态站点 重新加载按钮点击事件： 发送动态站点意图、加载动画淡入、按钮淡出
            siteReload.doOnClickInterval {
                mVM.input(AppIntent.GetDynamicSite())
                siteLoading.animateFadeIn()
                siteReload.animateFadeOutInVisibility()
            }

            // 静态站点按钮组点击事件：根据CheckedID设置全局URL的后缀、保存APP配置、延时关闭DIALOG
            siteStaticRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                mBaseEvent.doOnInterval {
                    when(checkedId) {
                        siteRadioOne.id -> BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_TV)
                        siteRadioTwo.id -> BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_SITE)
                    }
                    mVM.saveAppConfig(appConfig.copy(mCopyMangaSite = BaseStrings.URL.COPYMANGA))
                    mHandler.postDelayed({ mSiteAlertDialog?.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
                }
            }
        }

        // 打开弹窗后 发送获取动态站点意图
        mVM.input(AppIntent.GetDynamicSite())
    }

    private suspend fun initProxyView() {

        var appConfig = mVM.getReadedAppConfig() ?: return run { toast(getString(mangaR.string.mangax_unknow_error)) }

        val binding = MainSettingsProxyLayoutBinding.inflate(layoutInflater)

        val alertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(binding.root) }

        alertDialog.setOnDismissListener {
            val text = binding.proxyInputEdit.text
            appConfig = if (text == null) {
                appConfig.copy(mApiSecret = null)
            } else {
                appConfig.copy(mApiSecret = text.toString())
            }
            mVM.saveAppConfig(appConfig)
        }

        binding.close.doOnClickInterval { alertDialog.dismiss() }

        // 根据用户的路线 设置RadioButton的状态
        when(appConfig.mRoute) {
            "0" -> binding.proxyDomesticRoute.isChecked = true
            "1" -> binding.proxyOverseasRoute.isChecked = true
        }

        val regex = Regex("[A-Za-z0-9@]+")
        binding.proxyInputEdit.filters = arrayOf(object : InputFilter {
            override fun filter( source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
                // 只允许输入大写、小写字母和数字
                if (source != null && !source.matches(regex)) {
                    return ""
                }
                return null
            }
        })
        binding.proxySwitch.isChecked = mApiProxyEnable
        appConfig.mApiSecret?.let { binding.proxyInputEdit.setText(it) }

        // 代理组设置 选中监听
        binding.proxyRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            // 根据选中的RadioButton设置 用户路线
            when(checkedId) {
                binding.proxyDomesticRoute.id -> MangaXAccountConfig.mRoute = "0"
                binding.proxyOverseasRoute.id -> MangaXAccountConfig.mRoute = "1"
            }

            appConfig = appConfig.copy(mRoute = MangaXAccountConfig.mRoute)

            // 保存配置
            mVM.saveAppConfig(appConfig)

            // 延时关闭Dialog 让RadioButton选中后的过渡效果执行完毕
            mHandler.postDelayed({ alertDialog.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
        }

        binding.proxyInputEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) { }
            override fun onTextChanged(chars: CharSequence?, start: Int, before: Int, count: Int) {
                chars?.let {
                    if (it.length >= 20) {
                        appConfig = appConfig.copy(mApiSecret = it.toString())
                        mVM.saveAppConfig(appConfig)
                    }
                }
            }
        })

        binding.proxySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val text = binding.proxyInputEdit.text
                when {
                    text.isNullOrEmpty() -> { toast(getString(R.string.main_api_input_tips)) }
                    (text?.length ?: 0) < 20 -> { toast(getString(R.string.main_api_length_tips)) }
                    else -> { mApiProxyEnable = true }
                }
                mVM.saveAppCatLogConfig(SpNameSpace.Key.ENABLE_API_PROXY, true)
                return@setOnCheckedChangeListener
            }
            mApiProxyEnable = false
            mVM.saveAppCatLogConfig(SpNameSpace.Key.ENABLE_API_PROXY, false)
        }
    }

    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Settings.name)

    private fun navigateToStyleableFragment() {
        with(Fragments.Styleable.name) {
            parentFragmentManager.navigateToWithBackStack(
                id = mangaR.id.app_main_fcv,
                hideTarget = this@SettingsFragment,
                addedTarget = get(named((this))),
                tag = this,
                backStackName = this
            )
        }
    }

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun getViewBinding(inflater: LayoutInflater) =  MainFragmentSettingsBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.root)

        // 初始化Rv适配器
        mBinding.list.adapter = mAdapter
    }

    override fun initData(savedInstanceState: Bundle?) {
        val config = CatlogConfig.getCatlogConfigSp()
        mAdapter.submitList(
            mutableListOf(
                SettingTitleEntity(mID = -1,mTitle = getString(R.string.main_settings_title_basic)),
                SettingContentEntity(mID = 0, mResource = R.drawable.main_ic_personalise_24dp, mContent = getString(R.string.main_settings_style)),
                SettingContentEntity(mID = 1, mResource = R.drawable.main_ic_site_24dp, mContent = getString(R.string.main_settings_site)),
                SettingContentEntity(mID = 2, mResource = R.drawable.main_ic_proxy_24dp, mContent = getString(R.string.main_settings_proxy)),
                SettingContentEntity(mID = 3, mResource = R.drawable.main_ic_resolution_24dp, mContent = getString(R.string.main_settings_resolution)),
                SettingTitleEntity(mID = 4, mTitle = getString(R.string.main_settings_title_genric)),
                SettingSwitchEntity(mID = 5, mContent = "繁体转简体", mEnable = config.getBoolean(SpNameSpace.Key.ENABLE_CHINESE_CONVERT, mChineseConvert)),
                SettingSwitchEntity(mID = 7, mContent = "书架更新前置", mEnable = config.getBoolean(SpNameSpace.Key.ENABLE_UPDATE_PREFIX, mUpdatePrefix)),
                SettingSwitchEntity(mID = 6, mContent = "热度精准显示", mEnable = config.getBoolean(SpNameSpace.Key.ENABLE_HOT_ACCURATE_DISPLAY, mHotAccurateDisplay)),
                SettingSwitchEntity(mID = 8, mContent = "封面原图显示", mEnable = config.getBoolean(SpNameSpace.Key.ENABLE_COVER_ORINAL, mCoverOrinal)),
            )
        )
        mVM.input(AppIntent.GetDynamicSite())
    }

    override fun initListener() {
        mBinding.toolbar.navigateIconClickGap { navigateUp() }
    }

    override fun initObserver(saveInstanceState: Bundle?) {

        val baseEvent = BaseEvent.newInstance()

        mVM.onOutput { intent ->
            when(intent) {
                is AppIntent.GetDynamicSite -> {
                    intent.mViewState
                        .doOnError { _, _ ->
                            if (mSiteDialogBinding != null) {
                                // 加载失败 等待一段间隔后 利用Hanlder 延时处理行为 重新加载Button淡入 加载动画淡出
                                baseEvent.doOnInterval(mHandler) {
                                    mSiteDialogBinding?.siteReload?.animateFadeIn()
                                    mSiteDialogBinding?.siteLoading?.animateFadeOut()?.withEndAction { mSiteDialogBinding?.siteLoading?.isInvisible = false }
                                }
                            }
                        }
                        .doOnResult {
                            if (mSiteDialogBinding == null) return@doOnResult
                            baseEvent.doOnInterval(mHandler) {

                                // 遍历站点列表
                                intent.siteResp!!.mSiteList?.forEach { site ->

                                    // 解码站点
                                    val decodeSite = Base64.decode(site!!.mEncodeSite, Base64.DEFAULT).decodeToString()

                                    // 添加RadioButton To RadioGroup
                                    mSiteDialogBinding?.siteRadioGroup?.addView(MaterialRadioButton(mContext).also { button ->

                                        // 设置Button Width Match
                                        button.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                                        // 站点链接 和 解码站点链接相同
                                        if((BaseStrings.URL.COPYMANGA == decodeSite)) {

                                            button.isChecked = true

                                            // 静态站点选中按钮ID为-1代表 选中为空 此时给已选中的标题设置 当前获取数据的站点名
                                            if (mSiteDialogBinding!!.siteStaticRadioGroup.checkedRadioButtonId == -1) {
                                                mSiteDialogBinding!!.siteCurrent.text = getString(R.string.main_site_current, site.mName)
                                                mSiteDialogBinding!!.siteCurrent.animateFadeIn()
                                            }
                                        }

                                        // 粗体
                                        button.typeface = Typeface.DEFAULT_BOLD

                                        // 按钮名称 为 站点名称
                                        button.text = site.mName

                                        // 利用tag存储 解码的站点
                                        button.tag = decodeSite

                                        /*
                                        * 1：这一段处理逻辑 -> radioButton 被选中添加到group后会触发回调 需要手动处理逻辑
                                        * 按钮选中后遍历group中的子View根据当前选中的button和子View的id做比较，不等则吧button选中状态为false
                                        * 2：获取tag的解码站点赋值给公共URL
                                        * 3：保存配置
                                        * 4：Handler延时关闭dialog 让选中的radiobutton有一个过渡效果
                                        * */
                                        button.setOnCheckedChangeListener { buttonView, isChecked ->
                                            if (isChecked) {
                                                mSiteDialogBinding!!.siteRadioGroup.forEach { childView -> if (buttonView.id != (childView as MaterialRadioButton).id) childView.isChecked = false }
                                                BaseStrings.URL.COPYMANGA = buttonView.tag.toString()
                                                mVM.saveAppConfig()
                                                mHandler.postDelayed({ mSiteAlertDialog?.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
                                            }
                                        }
                                    })
                                }

                                // 加载动画淡出 动态站点Title、RadioGroup 淡入
                                mSiteDialogBinding?.siteLoading?.animateFadeOutGone()
                                mSiteDialogBinding?.siteTitle?.animateFadeIn()
                                mSiteDialogBinding?.siteRadioGroup?.animateFadeIn()
                            }
                        }
                }
            }
        }
    }
}