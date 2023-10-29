package com.crow.copymanga.model.di

import androidx.fragment.app.Fragment
import com.crow.base.copymanga.entity.Fragments
import com.crow.module_anime.ui.fragment.AnimeInfoFragment
import com.crow.module_book.ui.fragment.comic.BookComicFragment
import com.crow.module_book.ui.fragment.comic.reader.BookClassicComicFragment
import com.crow.module_book.ui.fragment.novel.BookNovelFragment
import com.crow.module_home.ui.fragment.TopicFragment
import com.crow.module_main.ui.fragment.AboutAuthorFragment
import com.crow.module_main.ui.fragment.ContainerFragment
import com.crow.module_main.ui.fragment.HistoryFragment
import com.crow.module_main.ui.fragment.ImageFragment
import com.crow.module_main.ui.fragment.SettingsFragment
import com.crow.module_main.ui.fragment.StyleableFragment
import com.crow.module_main.ui.fragment.UpdateHistoryFragment
import com.crow.module_mine.ui.fragment.MineBottomSheetFragment
import com.crow.module_mine.ui.fragment.MineIconFragment
import com.crow.module_mine.ui.fragment.MineLoginFragment
import com.crow.module_mine.ui.fragment.MineRegFragment
import com.crow.module_mine.ui.fragment.MineUpdateInfoFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.core.qualifier.named
import org.koin.dsl.module

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/kotlin/com/crow/copymanga/di
 * @Time: 2023/4/11 13:15
 * @Author: CrowForKotlin
 * @Description: FragmentModule
 * @formatter:on
 **************************/
val fragmentModule = module {
    fragment<BottomSheetDialogFragment>(named(Fragments.User.name)) { MineBottomSheetFragment() }
    fragment<Fragment>(named(Fragments.Container.name)) { ContainerFragment() }
    fragment<Fragment>(named(Fragments.MineInfo.name)) { MineUpdateInfoFragment() }
    fragment<Fragment>(named(Fragments.Login.name)) { MineLoginFragment() }
    fragment<Fragment>(named(Fragments.Reg.name)) { MineRegFragment() }
    fragment<Fragment>(named(Fragments.Icon.name)) { MineIconFragment() }
    fragment<Fragment>(named(Fragments.About.name)) { AboutAuthorFragment() }
    fragment<Fragment>(named(Fragments.Settings.name)) { SettingsFragment() }
    fragment<Fragment>(named(Fragments.BookComicInfo.name)) { BookComicFragment() }
    fragment<Fragment>(named(Fragments.BookNovelInfo.name)) { BookNovelFragment() }
    fragment<Fragment>(named(Fragments.Image.name)) { ImageFragment() }
    fragment<Fragment>(named(Fragments.Styleable.name)) { StyleableFragment() }
    fragment<Fragment>(named(Fragments.UpdateHistory.name)) { UpdateHistoryFragment() }
    fragment<Fragment>(named(Fragments.ComicClassic.name)) { BookClassicComicFragment() }
    fragment<Fragment>(named(Fragments.History.name)) { HistoryFragment() }
    fragment<Fragment>(named(Fragments.AnimeInfo.name)) { AnimeInfoFragment() }
    fragment<Fragment>(named(Fragments.Topic.name)) { TopicFragment() }
//    fragment<Fragment>(named(Fragments.ComicStript.name)) { BookStriptComicFragment() }
}