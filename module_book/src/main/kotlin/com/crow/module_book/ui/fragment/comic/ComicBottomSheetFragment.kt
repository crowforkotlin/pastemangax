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
import com.crow.base.ui.fragment.BaseMviBottomSheetDialogFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import com.crow.mangax.copymanga.entity.CatlogConfig
import com.crow.module_book.databinding.BookFragmentComicBottomBinding
import com.crow.module_book.model.entity.comic.reader.ReaderEvent
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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
                    isAppearanceLightNavigationBars = !CatlogConfig.mDarkMode
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
                ComicCategories.Type.PAGE_HORIZONTAL -> {
                    mBinding.buttonPageHorizontal.isChecked = true
                }
                ComicCategories.Type.PAGE_VERTICAL -> {
                    mBinding.buttonPageVertical.isChecked = true
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
                        mBinding.buttonPageHorizontal.id -> {
                            mBinding.buttonPageHorizontal.isChecked = true
                        }
                        mBinding.buttonPageVertical.id -> {
                            mBinding.buttonPageVertical.isChecked = true
                        }
                    }
                }
                override fun onIntervalOk(baseEventEntity: BaseEventEntity<BaseEvent>) {
                    checkedId = id
                    if (isChecked) {
                        when(id) {
                            mBinding.buttonStandard.id -> { sendOptionResult(ComicCategories.Type.STANDARD.id) }
                            mBinding.buttonStript.id -> { sendOptionResult(ComicCategories.Type.STRIPT.id) }
                            mBinding.buttonPageHorizontal.id -> { sendOptionResult(ComicCategories.Type.PAGE_HORIZONTAL.id) }
                            mBinding.buttonPageVertical.id -> { sendOptionResult(ComicCategories.Type.PAGE_VERTICAL.id)}
                        }
                    }
                }
            })
        }
    }

    private fun sendOptionResult(type: Int) {
        parentFragmentManager.setFragmentResult(ComicActivity.ACTIVITY_OPTION,
            bundleOf(
                ComicActivity.EVENT to ReaderEvent.READER_MODE,
                ComicActivity.VALUE to type
            )
        )
    }
}