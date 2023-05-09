package com.crow.base.copymanga.entity

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.get
import org.koin.core.qualifier.named

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/current_project/entity
 * @Time: 2023/4/11 13:19
 * @Author: CrowForKotlin
 * @Description: Fragments
 * @formatter:on
 **************************/
enum class Fragments {
    Container, Icon, Login, Reg, About, BookComicInfo, BookNovelInfo, UserInfo, User, Settings
}

inline fun Fragment.homeNavigateTo(enum: Enum<Fragments>, crossinline doOnBundle: Bundle.() -> Unit) {
    val bundle = Bundle().doOnBundle()
    val fragment = get<Fragment>(named(enum))
    val container = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.toString())!!
    val fragmentTag = fragment.toString()

}