package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import com.crow.base.app.appContext
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.current_project.entity.Fragments
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.appConfigDataStore
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentSettingsBinding
import com.crow.module_main.databinding.MainSettingsProxyLayoutBinding
import com.crow.module_main.databinding.MainSettingsSiteLayoutBinding
import com.crow.module_main.model.entity.MainAppConfigEntity
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.ui.adapter.SettingsAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.adapter.Res
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

class SettingsFragment : BaseMviFragment<MainFragmentSettingsBinding>() {

    private val mSettingsList = mutableListOf<Pair<Res?, String>>(
        R.drawable.main_ic_personalise_24dp to appContext.getString(R.string.main_settings_style),
        R.drawable.main_ic_site_24dp to appContext.getString(R.string.main_settings_site),
        R.drawable.main_ic_proxy_24dp to appContext.getString(R.string.main_settings_proxy),
    )
    private val screenHeight by lazy { mContext.resources.displayMetrics.heightPixels / 6 }
    private val mContainerVM by viewModel<ContainerViewModel>()
    private var mSiteAlertDialog: AlertDialog? = null
    private var mSiteDialogBinding: MainSettingsSiteLayoutBinding? = null

    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Settings.toString())

    private suspend fun getReadedAppConfig() : MainAppConfigEntity? {
        return viewLifecycleOwner.lifecycleScope.async { mContext.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG) }.await().toTypeEntity<MainAppConfigEntity>()
    }

    private fun saveConfig() {
        lifecycleScope.launch {
            mContext.appConfigDataStore.asyncEncode(DataStoreAgent.APP_CONFIG, toJson(MainAppConfigEntity()))
        }
    }

    private suspend fun initSiteView() {

        val readedAppConfigEntity = getReadedAppConfig() ?: return run { toast(getString(baseR.string.BaseUnknowError)) }

        mSiteDialogBinding = MainSettingsSiteLayoutBinding.inflate(layoutInflater)
        mSiteDialogBinding?.apply {
            mSiteAlertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(root) }
            mSiteAlertDialog?.setOnDismissListener { mSiteAlertDialog = null }

            if (readedAppConfigEntity.mSite.endsWith(BaseStrings.URL.CopyManga_TLD_TV)) {
                settingsSiteStaticRadioOne.isChecked = true
                settingsSiteCurrent.text = getString(R.string.main_site_current, settingsSiteStaticRadioOne.text)
            }

            else if (readedAppConfigEntity.mSite.endsWith(BaseStrings.URL.CopyManga_TLD_SITE)) {
                settingsSiteStaticRadioTwo.isChecked = true
                settingsSiteCurrent.text = getString(R.string.main_site_current, settingsSiteStaticRadioTwo.text)
            }

            else { settingsSiteCurrent.text = getString(R.string.main_site_current, getString(baseR.string.BaseUnknow)) }

            (settingsSiteDynamicScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
            (settingsSiteStaticScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight

            settingsSiteDynamicReload.doOnClickInterval {
                mContainerVM.input(ContainerIntent.GetSite())
                settingsSiteLoadingLottie.animateFadeIn()
                settingsSiteDynamicReload.animateFadeOut().withEndAction { settingsSiteDynamicReload.isInvisible = true }
            }
            settingsSiteStaticRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId) {
                    settingsSiteStaticRadioOne.id -> BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_TV)
                    settingsSiteStaticRadioTwo.id -> BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_SITE)
                }
                saveConfig()
                mHandler.postDelayed({ mSiteAlertDialog?.dismiss() },BaseEvent.BASE_FLAG_TIME)
            }


        }
        mContainerVM.input(ContainerIntent.GetSite())
    }

    private suspend fun initProxyView() {

        val readedAppConfigEntity = getReadedAppConfig() ?: return run { toast(getString(baseR.string.BaseUnknowError)) }

        val binding = MainSettingsProxyLayoutBinding.inflate(layoutInflater)

        val alertDialog = mContext.newMaterialDialog { dialog -> dialog.setView(binding.root) }

        when(BaseUser.CURRENT_REGION) {
            "0" -> binding.settingsProxyDomesticRoute.isChecked = true
            "1" -> binding.settingsProxyOverseasRoute.isChecked = true
        }

        binding.settingsProxyRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                binding.settingsProxyDomesticRoute.id -> BaseUser.CURRENT_REGION = "0"
                binding.settingsProxyOverseasRoute.id -> BaseUser.CURRENT_REGION = "1"
            }
            saveConfig()
            mHandler.postDelayed({ alertDialog.dismiss() },BaseEvent.BASE_FLAG_TIME)
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) =  MainFragmentSettingsBinding.inflate(inflater)

    override fun initView(bundle: Bundle?) {

        mBinding.root.immersionPadding()

        mBinding.settingsRv.adapter = SettingsAdapter(mSettingsList) { pos ->
            when(pos) {
                0 -> {
                    toast(getString(baseR.string.BaseStillInDevelopment))
                }
                1 -> viewLifecycleOwner.lifecycleScope.launch { initSiteView() }
                2 -> viewLifecycleOwner.lifecycleScope.launch { initProxyView() }
            }
        }
    }

    override fun initData() { mContainerVM.input(ContainerIntent.GetSite()) }

    override fun initListener() {
        mBinding.settingsBack.doOnClickInterval { navigateUp() }
    }

    override fun initObserver() {

        val baseEvent = BaseEvent.newInstance()

        mContainerVM.onOutput { intent ->
            when(intent) {
                is ContainerIntent.GetSite -> {
                    intent.mViewState
                        .doOnLoading {

                        }
                        .doOnError { _, _ ->
                            mSiteDialogBinding?.also { binding ->
                                baseEvent.doOnInterval(mHandler) {
                                    binding.settingsSiteDynamicReload.animateFadeIn()
                                    binding.settingsSiteLoadingLottie.animateFadeOut().withEndAction { binding.settingsSiteLoadingLottie.isInvisible = false }
                                }
                            }
                        }
                        .doOnResult {
                            if (mSiteDialogBinding == null) return@doOnResult
                            baseEvent.doOnInterval(mHandler) {
                                intent.siteResp!!.mSiteList?.forEach { site ->
                                    val decodeSite = Base64.decode(site!!.mEncodeSite, Base64.DEFAULT).decodeToString()
                                    mSiteDialogBinding!!.settingsSiteDynamicRadioGroup.addView(MaterialRadioButton(mContext).also { button ->
                                        button.text = site.mName
                                        button.tag = decodeSite
                                        if((BaseStrings.URL.CopyManga == decodeSite)) {
                                            button.isChecked = true
                                            if (mSiteDialogBinding!!.settingsSiteStaticRadioGroup.checkedRadioButtonId == -1) {
                                                mSiteDialogBinding!!.settingsSiteCurrent.text = getString(R.string.main_site_current, button.text)
                                                mSiteDialogBinding!!.settingsSiteCurrent.animateFadeIn()
                                            }
                                        }
                                        button.setOnCheckedChangeListener { buttonView, isChecked ->
                                            if (isChecked) {
                                                mSiteDialogBinding!!.settingsSiteDynamicRadioGroup.forEach {
                                                    if (buttonView.id != (it as MaterialRadioButton).id) {
                                                        it.isChecked = false
                                                    }
                                                }
                                                BaseStrings.URL.CopyManga = buttonView.tag.toString()
                                                saveConfig()
                                                mHandler.postDelayed({ mSiteAlertDialog?.dismiss() },BaseEvent.BASE_FLAG_TIME)
                                            }
                                        }
                                    })
                                }
                                mSiteDialogBinding!!.settingsSiteLoadingLottie.animateFadeOut().withEndAction { mSiteDialogBinding!!.settingsSiteLoadingLottie.isInvisible = true }
                                mSiteDialogBinding!!.settingsSiteDynamicTitle.animateFadeIn()
                                mSiteDialogBinding!!.settingsSiteDynamicRadioGroup.animateFadeIn()
                            }
                        }
                }
            }
        }
    }
}