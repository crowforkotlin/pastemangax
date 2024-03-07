package com.crow.module_book.ui.fragment.comic.reader

import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.tools.extensions.navigate
import com.crow.module_book.R
import com.crow.module_book.ui.activity.ComicActivity
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named

/**
 * ⦁ 漫画类别
 *
 * ⦁ 2023-11-05 02:25:40 周日 上午
 * @author crowforkotlin
 */
class ComicCategories(private val mActivity: ComicActivity, private val host: FragmentContainerView) {

    /**
     * ⦁ StaticArea
     *
     * ⦁ 2023-11-05 02:25:16 周日 上午
     * @author crowforkotlin
     */
    companion object {
        const val CATEGORIES = "CATEGORIES"
        var CURRENT_TYPE = Type.STRIPT
            private set
    }

    /**
     * ⦁ 类型
     *
     * ⦁ 2023-11-05 02:25:52 周日 上午
     * @author crowforkotlin
     */
    enum class Type(@IdRes val id: Int) {
        STRIPT(R.string.book_comic_stript),
        STANDARD(R.string.book_comic_standard),
        PAGE(R.string.book_comic_page),
    }


    /**
     * ⦁ 应用
     *
     * ⦁ 2023-11-05 02:26:01 周日 上午
     * @author crowforkotlin
     */
    fun apply(type: Type) {
        CURRENT_TYPE = type
        when (type) {
            Type.STRIPT -> mActivity.supportFragmentManager.navigate(host.id, ComicStriptFragment())
            Type.STANDARD -> mActivity.supportFragmentManager.navigate(host.id, ComicStandardFragment())
            Type.PAGE -> { mActivity.supportFragmentManager.navigate(host.id, mActivity.get<Fragment>(named(Fragments.ComicPage.name)).also { it.arguments = bundleOf("CATEGORIES" to type.id) }) }
        }
    }
}