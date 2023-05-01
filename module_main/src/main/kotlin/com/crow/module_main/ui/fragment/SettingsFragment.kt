package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.entity.Fragments
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.appConfigDataStore
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentSettingsBinding
import com.crow.module_main.databinding.MainSettingsSiteLayoutBinding
import com.crow.module_main.model.entity.MainAppConfigEntity
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.ui.adapter.SettingsAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.adapter.Res
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

class SettingsFragment : BaseMviFragment<MainFragmentSettingsBinding>() {

    private val mSettingsList = mutableListOf<Pair<Res?, String>>(
        R.drawable.main_ic_personalise_24dp to "个性化",
        R.drawable.main_ic_site_24dp to "配置站点",
        R.drawable.main_ic_proxy_24dp to "配置代理",
    )

    private val mContainerVM by viewModel<ContainerViewModel>()

    private var dialogBinding: MainSettingsSiteLayoutBinding? = null

    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Settings.toString())

    private suspend fun initSiteView() {
        val screenHeight = mContext.resources.displayMetrics.heightPixels / 6
        val readedAppConfigEntity = viewLifecycleOwner.lifecycleScope.async { mContext.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG) }.await().toTypeEntity<MainAppConfigEntity>() ?: return run {
            toast(getString(baseR.string.BaseUnknow))
            return
        }
        dialogBinding = MainSettingsSiteLayoutBinding.inflate(layoutInflater)
        dialogBinding?.apply {
            if (readedAppConfigEntity.mSite.endsWith(BaseStrings.URL.CopyManga_TLD_TV)) {
                settingsSiteStaticRadioOne.isChecked = true
            } else if (readedAppConfigEntity.mSite.endsWith(BaseStrings.URL.CopyManga_TLD_COM)) {
                settingsSiteStaticRadioTwo.isChecked = true
            } else if (readedAppConfigEntity.mSite.endsWith(BaseStrings.URL.CopyManga_TLD_SITE)) {
                settingsSiteStaticRadioThree.isChecked = true
            }
            (settingsSiteDynamicScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
            (settingsSiteStaticScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
            settingsSiteStaticRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId) {
                    settingsSiteStaticRadioOne.id -> {
                        BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_TV)
                    }
                    settingsSiteStaticRadioTwo.id -> {
                        BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_COM)
                    }
                    settingsSiteStaticRadioThree.id ->{
                        BaseStrings.URL.setCopyMangaUrl(BaseStrings.URL.CopyManga_TLD_SITE)
                    }
                }
                lifecycleScope.launch(Dispatchers.IO) { mContext.appConfigDataStore.asyncEncode(DataStoreAgent.APP_CONFIG, toJson(MainAppConfigEntity(false, BaseStrings.URL.CopyManga))) }
                getKoin().setProperty("base_url", BaseStrings.URL.CopyManga)
            }
            mContext.newMaterialDialog { dialog -> dialog.setView(root) }
        }
        mContainerVM.input(ContainerIntent.GetSite())
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
                2 -> {

                }
            }
        }
    }

    override fun initData() { mContainerVM.input(ContainerIntent.GetSite()) }

    override fun initListener() {
        mBinding.settingsBack.doOnClickInterval { navigateUp() }
    }

    override fun initObserver() {
        mContainerVM.onOutput { intent ->
            when(intent) {
                is ContainerIntent.GetSite -> {
                    intent.mViewState
                        .doOnLoading {

                        }
                        .doOnError { _, _ ->

                        }
                        .doOnResult {
                            dialogBinding?.also {  binding ->
                                binding.settingsSiteLoadingLottie.animateFadeOut().withEndAction { binding.settingsSiteLoadingLottie.isVisible = false }
                                binding.settingsSiteDynamicTitle.animateFadeIn()
                                binding.settingsSiteDynamicRadioGroup.animateFadeIn()
                                intent.siteResp!!.mSiteList?.forEach { site ->
                                    binding.settingsSiteDynamicRadioGroup.addView(MaterialRadioButton(mContext).also { button ->
                                        button.text = site!!.mName
                                    })
                                    binding.settingsSiteDynamicRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                                        BaseStrings.URL.CopyManga = Base64.decode(site!!.mEncodeSite, Base64.DEFAULT).decodeToString()

                                    }
                                }
                            }
                        }
                }
            }
        }
    }
}