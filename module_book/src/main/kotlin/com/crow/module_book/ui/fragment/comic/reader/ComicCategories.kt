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
class ComicCategories(
    private val mActivity: ComicActivity,
    private val host: FragmentContainerView
) {

    /**
     * ⦁ StaticArea
     *
     * ⦁ 2023-11-05 02:25:16 周日 上午
     * @author crowforkotlin
     */
    companion object {
        const val CATEGORIES = "CATEGORIES"
        var CURRENT_TYPE = Type.STRIPT
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
        PAGE_HORIZONTAL_LTR(R.string.book_comic_page_horizontal_ltr),
        PAGE_HORIZONTAL_RTL(R.string.book_comic_page_horizontal_rtl),
        PAGE_VERTICAL_TTB(R.string.book_comic_page_vertical_ttb),
        PAGE_VERTICAL_BTT(R.string.book_comic_page_vertical_btt),
    }


    /**
     * ⦁ 应用
     *
     * ⦁ 2023-11-05 02:26:01 周日 上午
     * @author crowforkotlin
     */
    fun apply(type: Type) {
        CURRENT_TYPE = type
        val hostId = host.id
        mActivity.supportFragmentManager.apply {
            when (type) {
                Type.STRIPT -> {
                    navigate(hostId, ComicStriptFragment())
                }
                Type.STANDARD -> {
                    navigate(hostId, ComicStandardFragment())
                }
                Type.PAGE_HORIZONTAL_LTR -> {
                    navigate(hostId,
                        mActivity.get<Fragment>(named(Fragments.ComicPageHorizontal.name))
                            .also { it.arguments = bundleOf("CATEGORIES" to type.id, "REVERSE" to false) })
                }
                Type.PAGE_VERTICAL_TTB -> {
                    navigate(
                        hostId,
                        mActivity.get<Fragment>(named(Fragments.ComicPageVertical.name))
                            .also { it.arguments = bundleOf("CATEGORIES" to type.id, "REVERSE" to false) })
                }
                Type.PAGE_HORIZONTAL_RTL -> {
                    navigate(hostId,
                        mActivity.get<Fragment>(named(Fragments.ComicPageHorizontal.name))
                            .also { it.arguments = bundleOf("CATEGORIES" to type.id, "REVERSE" to true) })
                }
                Type.PAGE_VERTICAL_BTT -> {
                    navigate(
                        hostId,
                        mActivity.get<Fragment>(named(Fragments.ComicPageVertical.name))
                            .also { it.arguments = bundleOf("CATEGORIES" to type.id, "REVERSE" to true) })
                }
            }
        }
    }
}
