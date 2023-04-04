package com.crow.module_discover.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @RelativePath: com\crow\module_main\ui\adapter\ContainerAdapter.kt
 * @Path: D:\Programing\Android\2023\CopyManga\module_main\src\main\kotlin\com\crow\module_main\ui\adapter\ContainerAdapter.kt
 * @Author: CrowForKotlin
 * @Time: 2023/3/21 17:35 Tue PM
 * @Description: ContainerAdapter
 * @formatter:on
 *************************/

class DiscoverAdapter(
    private val fragmentList: MutableList<Fragment>,
    fragmentManager: FragmentActivity,
) : FragmentStateAdapter(fragmentManager) {

    override fun getItemCount(): Int = fragmentList.size
    override fun createFragment(position: Int): Fragment = fragmentList[position]
}

