package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_main.databinding.MainFragmentImageBinding

class ImageFragment : BaseMviFragment<MainFragmentImageBinding>() {

    private var imageUrl: String? = null

    // WindowInsets属性 （状态栏属性设置等...）
    private var mWindowInsets: WindowInsetsControllerCompat? = null

    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Image.toString())

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentImageBinding.inflate(inflater)


    override fun initView(bundle: Bundle?) {

        // 初始化 设置状态栏暗色
        mWindowInsets = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
        mWindowInsets?.isAppearanceLightStatusBars = false

        imageUrl = arguments?.getString(BaseStrings.IMAGE_URL)

        if (imageUrl == null) return

        Glide.with(mContext)
            .load(imageUrl)
            .into(mBinding.mainImagePhotoview)
    }

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 恢复状态栏亮色 同时 置空（防止泄漏...?）
        mWindowInsets?.isAppearanceLightStatusBars = true
        mWindowInsets = null
    }
}