package com.crow.module_book.ui.fragment.comic.reader

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentContainerView
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.extensions.navigateByAdd
import com.crow.module_book.R
import com.crow.module_book.ui.activity.ComicActivity
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.fragment.comic
 * @Time: 2023/6/28 0:49
 * @Author: CrowForKotlin
 * @Description:
 * @formatter:on
 **************************/
class BookComicCategories(private val mActivity: ComicActivity ,private val host: FragmentContainerView) {

    enum class Type(@IdRes val id: Int) {
//        STRIPT(R.string.book_comic_stript),
        CLASSIC(R.string.book_comic_classic)
    }


    fun apply(type: Type) {
        when (type) {
//            Type.STRIPT -> mActivity.supportFragmentManager.navigateByAdd(host.id, mActivity.get(named(Fragments.ComicStript.name)))
            Type.CLASSIC -> mActivity.supportFragmentManager.navigateByAdd(host.id, mActivity.get(named(Fragments.ComicClassic.name)))
        }
    }
}