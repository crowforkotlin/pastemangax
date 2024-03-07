package com.crow.module_book.ui.activity

import androidx.core.os.bundleOf
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.module_book.databinding.BookActivityComicBinding

/**
 * ⦁ BaseComicActivity
 *
 * ⦁ 2024/3/4 01:59
 * @author crowforkotlin
 * @formatter:on
 */
abstract class BaseComicActivity :  BaseMviActivity<BookActivityComicBinding>(){

    override fun getViewBinding() = BookActivityComicBinding.inflate(layoutInflater)

    fun sendFragmentResult(event: Int, value: Any? = null) {
        if (value == null) {
            supportFragmentManager.setFragmentResult(ComicActivity.FRAGMENT_OPTION, bundleOf(ComicActivity.EVENT to event))
        } else {
            supportFragmentManager.setFragmentResult(ComicActivity.FRAGMENT_OPTION, bundleOf(ComicActivity.EVENT to event, ComicActivity.VALUE to value))
        }
    }
}