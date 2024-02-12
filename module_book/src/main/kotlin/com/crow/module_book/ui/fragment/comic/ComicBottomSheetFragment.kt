package com.crow.module_book.ui.fragment.comic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.ui.fragment.BaseMviBottomSheetDialogFragment
import com.crow.module_book.databinding.BookFragmentComicBottomBinding
import com.google.android.material.R

class ComicBottomSheetFragment : BaseMviBottomSheetDialogFragment<BookFragmentComicBottomBinding>() {
    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBottomBinding.inflate(layoutInflater)

    override fun onStart() {
        super.onStart()

        // 设置BottomSheet的 高度
        dialog?.findViewById<View>(R.id.design_bottom_sheet)?.apply {
            layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams!!.width = ViewGroup.LayoutParams.MATCH_PARENT
            immersionPadding(this, update = { view, insets, _ ->
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                    rightMargin = insets.right
                    bottomMargin = insets.bottom
                }
            })
        }

    }

    override fun initView(bundle: Bundle?) {

    }
}