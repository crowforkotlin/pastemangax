package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.updateLayoutParams
import com.crow.mangax.copymanga.entity.AppConfigEntity.Companion.mDarkMode
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentStyleableBinding
import com.crow.module_main.model.entity.StyleableEntity
import com.crow.module_main.ui.adapter.StyleableAdapter
import com.crow.module_main.ui.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StyleableFragment : BaseMviFragment<MainFragmentStyleableBinding>() {

    private val mContainerVM by viewModel<MainViewModel>()

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

    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.root) { view, insets, _ ->
            mBinding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
        }
    }

    override fun initListener() {

        mBinding.styleableToolbar.navigateIconClickGap { navigateUp() }

        mContainerVM.mAppConfig.onCollect(this) {

            if (it == null) return@onCollect

            if (mStyableAdapter == null) {
                mStyableAdapter = StyleableAdapter(getStyleableEntitys(mDarkMode)) { pos, isSwitch ->
                    if (pos == 0) {
                        showLoadingAnim()
                        val darkMode = if (isSwitch) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        mContainerVM.saveCatalogDarkModeEnable(darkMode)
                        mHandler.postDelayed({ dismissLoadingAnim { AppCompatDelegate.setDefaultNightMode(darkMode) } }, BASE_ANIM_300L)
                    }
                }
                mBinding.styleableRv.adapter = mStyableAdapter
            }
        }
    }
}