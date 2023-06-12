package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import com.crow.base.copymanga.entity.AppConfigEntity
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.appDarkMode
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentStyleableBinding
import com.crow.module_main.model.entity.StyleableEntity
import com.crow.module_main.ui.adapter.StyleableAdapter
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StyleableFragment : BaseMviFragment<MainFragmentStyleableBinding>() {

    private val mContainerVM by viewModel<ContainerViewModel>()

    private var mStyableAdapter: StyleableAdapter? = null

    /** ● 返回上一个界面  */
    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Styleable.name)


    private fun getStyleableEntitys(isDark: Boolean): MutableList<StyleableEntity> {
        return mutableListOf(
            StyleableEntity(getString(R.string.main_dark_mode),
                mIsChecked = isDark,
                mIsContentEnable = false,
                mIsSwitchEnable = true
            ),
        ).also { datas ->
            repeat(3) {
                datas.add(StyleableEntity("...",
                    mIsChecked = false,
                    mIsContentEnable = false,
                    mIsSwitchEnable = false
                ))
            }
        }
    }


    /** ● 获取ViewBinding */
    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentStyleableBinding.inflate(inflater)

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mStyableAdapter = null
    }

    override fun initView(bundle: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.immersionPadding()

    }

    override fun initListener() {

        mBinding.styleableToolbar.navigateIconClickGap { navigateUp() }

        mContainerVM.appConfig.onCollect(this) {

            if (it == null) return@onCollect

            if (mStyableAdapter == null) {
                mStyableAdapter = StyleableAdapter(getStyleableEntitys(appDarkMode == AppCompatDelegate.MODE_NIGHT_YES)) { pos, isSwitch ->
                    if (pos == 0) {
                        showLoadingAnim()
                        appDarkMode =  if (isSwitch) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        mContainerVM.saveAppConfig(AppConfigEntity(mDarkMode = appDarkMode))
                        mHandler.postDelayed({ dismissLoadingAnim { AppCompatDelegate.setDefaultNightMode(appDarkMode) } }, BASE_ANIM_300L)
                    }
                }
                mBinding.styleableRv.adapter = mStyableAdapter
            }
        }
    }
}