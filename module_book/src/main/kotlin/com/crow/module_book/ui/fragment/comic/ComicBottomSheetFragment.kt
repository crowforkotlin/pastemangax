package com.crow.module_book.ui.fragment.comic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.log
import com.crow.base.ui.fragment.BaseMviBottomSheetDialogFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventInterval
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.module_book.databinding.BookFragmentComicBottomBinding
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ComicBottomSheetFragment : BaseMviBottomSheetDialogFragment<BookFragmentComicBottomBinding>() {

    private val mVM by activityViewModel<ComicViewModel>()

    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBottomBinding.inflate(layoutInflater)

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            // 配置行为
            (dialog as BottomSheetDialog).apply {
                dismissWithAnimation = true
                behavior.saveFlags = BottomSheetBehavior.SAVE_ALL
            }

            // 沉浸式
            dialog.window?.let { window ->
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = ColorUtils.setAlphaComponent(Color.WHITE, 1)
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightNavigationBars = !AppConfig.mDarkMode
                }
            }

            // 设置BottomSheet的 高度
            dialog.findViewById<View>(R.id.design_bottom_sheet)?.apply {
                layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams!!.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
    }

    override fun initObserver(saveInstanceState: Bundle?) {
        mVM.mReaderSetting.apply {
            when(this?.mReadMode) {
                ComicCategories.Type.STANDARD -> {
                    mBinding.buttonStandard.isChecked = true
                }
                ComicCategories.Type.STRIPT -> {
                    mBinding.buttonStript.isChecked = true
                }
                ComicCategories.Type.PAGE -> {
                    mBinding.buttonPage.isChecked = true
                }
                else -> {
                    mBinding.buttonStandard.isChecked = true
                }
            }
        }
    }

    override fun initView(bundle: Bundle?) {

    }

    override fun initListener() {
        @IdRes var checkedId = -1
        mBinding.buttonToggle.addOnButtonCheckedListener { toggle, id, isChecked ->
            if(isChecked)
            BaseEvent.getSIngleInstance().doOnInterval(object : BaseIEventIntervalExt<BaseEvent> {
                override fun onIntervalFailure(gapTime: Long) {
                    when(checkedId) {
                        mBinding.buttonStandard.id -> {
                            mBinding.buttonStandard.isChecked = true
                        }
                        mBinding.buttonStript.id -> {
                            mBinding.buttonStript.isChecked = true
                        }
                        mBinding.buttonPage.id -> {
                            mBinding.buttonPage.isChecked = true
                        }
                    }
                }
                override fun onIntervalOk(baseEventEntity: BaseEventEntity<BaseEvent>) {
                    checkedId = id
                    if (isChecked) {
                        when(id) {
                            mBinding.buttonStandard.id -> { parentFragmentManager.setFragmentResult(ComicActivity.OPTION, bundleOf(ComicActivity.READER_MODE to ComicCategories.Type.STANDARD.id) ) }
                            mBinding.buttonStript.id -> { parentFragmentManager.setFragmentResult(ComicActivity.OPTION, bundleOf(ComicActivity.READER_MODE to ComicCategories.Type.STRIPT.id) ) }
                            mBinding.buttonPage.id -> { parentFragmentManager.setFragmentResult(ComicActivity.OPTION, bundleOf(ComicActivity.READER_MODE to ComicCategories.Type.PAGE.id) ) }
                        }
                    }
                }
            })
        }
    }
}