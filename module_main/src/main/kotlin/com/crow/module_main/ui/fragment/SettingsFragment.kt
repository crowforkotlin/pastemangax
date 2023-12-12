package com.crow.module_main.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
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
import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.BaseUserConfig
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisible
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.setCenterAnimWithFadeOut
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentSettingsBinding
import com.crow.module_main.databinding.MainSettingsProxyLayoutBinding
import com.crow.module_main.databinding.MainSettingsResolutionLayoutBinding
import com.crow.module_main.databinding.MainSettingsSiteLayoutBinding
import com.crow.module_main.model.intent.AppIntent
import com.crow.module_main.ui.adapter.SettingsAdapter
import com.crow.module_main.ui.viewmodel.MainViewModel
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import com.crow.base.R as baseR

class SettingsFragment : BaseMviFragment<MainFragmentSettingsBinding>() {

    /**
     * ● 屏幕高度 / 6
     *
     * ● 2023-10-02 23:27:58 周一 下午
     */
    private val mScreenHeight by lazy { mContext.resources.displayMetrics.heightPixels / 6 }

    /**
     * ● 容器VM
     *
     * ● 2023-10-02 23:28:07 周一 下午
     */
    private val mVM by viewModel<MainViewModel>()

    /**
     * ● 站点Dialog
     *
     * ● 2023-10-02 23:28:32 周一 下午
     */
    private var mSiteAlertDialog: AlertDialog? = null

    /**
     * ● 站点Dialog Binding
     *
     * ● 2023-10-02 23:28:42 周一 下午
     */
    private var mSiteDialogBinding: MainSettingsSiteLayoutBinding? = null

    /**
     * ● Global BaseEvent
     *
     * ● 2023-10-02 23:41:17 周一 下午
     */
    private val mBaseEvent by lazy { BaseEvent.getSIngleInstance() }

    /**
     * ● 点击Item
     *
     * ● 2023-10-02 23:26:28 周一 下午
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

    private suspend fun initResolution() {

        // 获取APP的配置
        val appConfig = mVM.getReadedAppConfig() ?: return run { toast(getString(baseR.string.BaseUnknowError)) }

        val binding = MainSettingsResolutionLayoutBinding.inflate(layoutInflater)

        val alertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(binding.root) }

        when(appConfig.mResolution) {
            800 -> binding.settingsResolution800.isChecked = true
            1200 -> binding.settingsResolution1200.isChecked = true
            1500 -> binding.settingsResolution1500.isChecked = true
        }

        binding.settingsResolutionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            mBaseEvent.doOnInterval {
                when(checkedId) {
                    binding.settingsResolution800.id -> BaseUserConfig.RESOLUTION = 800
                    binding.settingsResolution1200.id -> BaseUserConfig.RESOLUTION = 1200
                    binding.settingsResolution1500.id -> BaseUserConfig.RESOLUTION = 1500
                }
                mVM.saveAppConfig(appConfig.copy(mResolution = BaseUserConfig.RESOLUTION))
                mHandler.postDelayed({ alertDialog.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
            }
        }
    }

    private suspend fun initSiteView() {
        // 获取APP的配置
        val appConfig = mVM.getReadedAppConfig() ?: return run { toast(getString(baseR.string.BaseUnknowError)) }

        mSiteDialogBinding = MainSettingsSiteLayoutBinding.inflate(layoutInflater)

        mSiteDialogBinding?.apply {
            mSiteAlertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(root) }

            // 取消弹窗的时候吧 局部AlerDialog和DialogBinding置空 防止泄漏
            mSiteAlertDialog?.setOnDismissListener {
                mSiteDialogBinding = null
                mSiteAlertDialog = null
            }

            // TV后缀设置当前站点为...
            if (appConfig.mCopyMangaSite.endsWith(BaseStrings.URL.CopyManga_TLD_TV)) {
                settingsSiteStaticRadioOne.isChecked = true
                settingsSiteCurrent.text = getString(R.string.main_site_current, settingsSiteStaticRadioOne.text)
            }

            // SITE后缀设置当前站点为...
            else if (appConfig.mCopyMangaSite.endsWith(BaseStrings.URL.CopyManga_TLD_SITE)) {
                settingsSiteStaticRadioTwo.isChecked = true
                settingsSiteCurrent.text = getString(R.string.main_site_current, settingsSiteStaticRadioTwo.text)
            }

            // 否则 当前站点位置
            else { settingsSiteCurrent.text = getString(R.string.main_site_current, getString(baseR.string.BaseUnknow)) }

            // 设置 静态、动态站点的ScrollView最大高度为 屏幕高度像素 / 6
            (settingsSiteStaticScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = mScreenHeight
            (settingsSiteDynamicScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = mScreenHeight

            // 动态站点 重新加载按钮点击事件： 发送动态站点意图、加载动画淡入、按钮淡出
            settingsSiteDynamicReload.doOnClickInterval {
                mVM.input(AppIntent.GetDynamicSite())
                settingsSiteLoadingLottie.animateFadeIn()
                settingsSiteDynamicReload.animateFadeOutWithEndInVisibility()
            }

            // 静态站点按钮组点击事件：根据CheckedID设置全局URL的后缀、保存APP配置、延时关闭DIALOG
            settingsSiteStaticRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                mBaseEvent.doOnInterval {
                    when(checkedId) {
                        settingsSiteStaticRadioOne.id -> BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_TV)
                        settingsSiteStaticRadioTwo.id -> BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_SITE)
                    }
                    mVM.saveAppConfig(appConfig.copy(mCopyMangaSite = BaseStrings.URL.COPYMANGA))
                    mHandler.postDelayed({ mSiteAlertDialog?.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
                }
            }
        }

        // 打开弹窗后 发送获取动态站点意图
        mVM.input(AppIntent.GetDynamicSite())
    }

    private fun initProxyView() {

        val binding = MainSettingsProxyLayoutBinding.inflate(layoutInflater)
        val alertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(binding.root) }

        // 根据用户的路线 设置RadioButton的状态
        when(BaseUserConfig.CURRENT_ROUTE) {
            "0" -> binding.settingsProxyDomesticRoute.isChecked = true
            "1" -> binding.settingsProxyOverseasRoute.isChecked = true
        }

        // 代理组设置 选中监听
        binding.settingsProxyRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            // 根据选中的RadioButton设置 用户路线
            when(checkedId) {
                binding.settingsProxyDomesticRoute.id -> BaseUserConfig.CURRENT_ROUTE = "0"
                binding.settingsProxyOverseasRoute.id -> BaseUserConfig.CURRENT_ROUTE = "1"
            }

            // 保存配置
            mVM.saveAppConfig()

            // 延时关闭Dialog 让RadioButton选中后的过渡效果执行完毕
            mHandler.postDelayed({ alertDialog.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
        }
    }

    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Settings.name)

    private fun navigateToStyleableFragment() {
        with(Fragments.Styleable.name) {
            parentFragmentManager.navigateToWithBackStack(baseR.id.app_main_fcv, this@SettingsFragment, get(named((this))), this, this, transaction = { it.setCenterAnimWithFadeOut() })
        }
    }

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun getViewBinding(inflater: LayoutInflater) =  MainFragmentSettingsBinding.inflate(inflater)

    override fun initView(bundle: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.root)

        mBinding.settingsRv.adapter = SettingsAdapter(mutableListOf(
            R.drawable.main_ic_personalise_24dp to app.getString(R.string.main_settings_style),
            R.drawable.main_ic_site_24dp to app.getString(R.string.main_settings_site),
            R.drawable.main_ic_proxy_24dp to app.getString(R.string.main_settings_proxy),
            R.drawable.main_ic_resolution_24dp to app.getString(R.string.main_settings_resolution),
        )) { pos -> onClickItem(pos) }
    }

    override fun initData(savedInstanceState: Bundle?) { mVM.input(AppIntent.GetDynamicSite()) }

    override fun initListener() {
        mBinding.settingsToolbar.navigateIconClickGap { navigateUp() }
    }

    override fun initObserver(savedInstanceState: Bundle?) {

        val baseEvent = BaseEvent.newInstance()

        mVM.onOutput { intent ->
            when(intent) {
                is AppIntent.GetDynamicSite -> {
                    intent.mViewState
                        .doOnError { _, _ ->
                            if (mSiteDialogBinding != null) {
                                // 加载失败 等待一段间隔后 利用Hanlder 延时处理行为 重新加载Button淡入 加载动画淡出
                                baseEvent.doOnInterval(mHandler) {
                                    mSiteDialogBinding?.settingsSiteDynamicReload?.animateFadeIn()
                                    mSiteDialogBinding?.settingsSiteLoadingLottie?.animateFadeOut()?.withEndAction { mSiteDialogBinding?.settingsSiteLoadingLottie?.isInvisible = false }
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
                                    mSiteDialogBinding?.settingsSiteDynamicRadioGroup?.addView(MaterialRadioButton(mContext).also { button ->

                                        // 设置Button Width Match
                                        button.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                                        // 站点链接 和 解码站点链接相同
                                        if((BaseStrings.URL.COPYMANGA == decodeSite)) {

                                            button.isChecked = true

                                            // 静态站点选中按钮ID为-1代表 选中为空 此时给已选中的标题设置 当前获取数据的站点名
                                            if (mSiteDialogBinding!!.settingsSiteStaticRadioGroup.checkedRadioButtonId == -1) {
                                                mSiteDialogBinding!!.settingsSiteCurrent.text = getString(R.string.main_site_current, site.mName)
                                                mSiteDialogBinding!!.settingsSiteCurrent.animateFadeIn()
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
                                                mSiteDialogBinding!!.settingsSiteDynamicRadioGroup.forEach { childView -> if (buttonView.id != (childView as MaterialRadioButton).id) childView.isChecked = false }
                                                BaseStrings.URL.COPYMANGA = buttonView.tag.toString()
                                                mVM.saveAppConfig()
                                                mHandler.postDelayed({ mSiteAlertDialog?.dismiss() },BaseEvent.BASE_FLAG_TIME_500)
                                            }
                                        }
                                    })
                                }

                                // 加载动画淡出 动态站点Title、RadioGroup 淡入
                                mSiteDialogBinding?.settingsSiteLoadingLottie?.animateFadeOutWithEndInVisible()
                                mSiteDialogBinding?.settingsSiteDynamicTitle?.animateFadeIn()
                                mSiteDialogBinding?.settingsSiteDynamicRadioGroup?.animateFadeIn()
                            }
                        }
                }
            }
        }
    }
}