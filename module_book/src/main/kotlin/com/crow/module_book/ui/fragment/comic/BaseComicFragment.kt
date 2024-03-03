package com.crow.module_book.ui.fragment.comic

import androidx.core.os.bundleOf
import androidx.viewbinding.ViewBinding
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_book.model.entity.comic.reader.ReaderEvent
import com.crow.module_book.ui.activity.ComicActivity

/**
 * ● BaseComicFragment
 *
 * ● 2024/3/4 01:53
 * @author crowforkotlin
 * @formatter:on
 */
abstract class  BaseComicFragment<VB: ViewBinding> : BaseMviFragment<VB>() {


    fun sendActivityResult(event: Int, value: Any? = null) {
        if (value == null) {
            parentFragmentManager.setFragmentResult(ComicActivity.ACTIVITY_OPTION, bundleOf(ComicActivity.EVENT to event))
        } else {
            parentFragmentManager.setFragmentResult(ComicActivity.ACTIVITY_OPTION, bundleOf(ComicActivity.EVENT to event, ComicActivity.VALUE to value))
        }
    }
}