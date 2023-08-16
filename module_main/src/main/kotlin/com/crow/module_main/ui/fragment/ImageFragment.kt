package com.crow.module_main.ui.fragment

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.crow.base.app.appContext
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentImageBinding
import com.crow.module_main.ui.viewmodel.ImageViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImageFragment : BaseMviFragment<MainFragmentImageBinding>() {

    private var mImageUrl: String? = null
    private var mImageName: String? = null

    // WindowInsets属性 （状态栏属性设置等...）
    private var mWindowInsets: WindowInsetsControllerCompat? = null

    /**
     * ● ImageVM
     *
     * ● 2023-07-02 21:49:02 周日 下午
     */
    private val mImageVM: ImageViewModel by viewModel()

    private val mActivitResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            saveBitmapToDCIM()
        } else {
            toast(getString(R.string.main_permission_tips))
            if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) || !shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { it.data = Uri.parse("package:${appContext.packageName}") })
            }
        }
    }

    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Image.name)

    private fun saveBitmapToDCIM() {
        viewLifecycleOwner.lifecycleScope.launch {
            mImageVM.saveBitmapToDCIM((mBinding.mainImagePhotoview.drawable as BitmapDrawable).bitmap, mImageName!!).collect { state ->
                state.second
                    .doOnLoading { showLoadingAnim() }
                    .doOnResult {
                        dismissLoadingAnim {
                            toast("图片已保存至：${state.first}")
                        }
                    }
                    .doOnError { _, _ ->
                        dismissLoadingAnim {
                            toast("保存失败，请重试...")
                        }
                    }
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentImageBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {

        immersionPadding(mBinding.root)

        // 初始化 设置状态栏暗色
        mWindowInsets = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
        mWindowInsets?.isAppearanceLightStatusBars = false

        mImageUrl = arguments?.getString(BaseStrings.IMAGE_URL)
        mImageName = arguments?.getString("name")

        if (mImageUrl == null) return

        Glide.with(mContext)
            .load(mImageUrl)
            .into(mBinding.mainImagePhotoview)
    }

    override fun initListener() {
        mBinding.mainImageDownload.doOnClickInterval {
            if (mImageUrl == null || mImageName == null) return@doOnClickInterval
            mActivitResult.launch(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }
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