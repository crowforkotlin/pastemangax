package com.crow.module_main.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.request.ImageRequest
import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.toast
import com.crow.base.tools.extensions.updatePadding
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentImageBinding
import com.crow.module_main.ui.viewmodel.ImageViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * ● 图片Fragemnt
 *
 * ● 2024-01-08 22:43:03 周一 下午
 * @author crowforkotlin
 */
class ImageFragment : BaseMviFragment<MainFragmentImageBinding>() {

    /**
     * ● 图片URL
     *
     * ● 2024-01-08 22:43:24 周一 下午
     * @author crowforkotlin
     */
    private var mImageUrl: String? = null

    /**
     * ● 图片名称
     *
     * ● 2024-01-08 22:43:51 周一 下午
     * @author crowforkotlin
     */
    private var mImageName: String? = null

    /**
     * ● WindowInsets属性 （状态栏属性设置等...）
     *
     * ● 2024-01-08 22:44:44 周一 下午
     * @author crowforkotlin
     */
    private var mWindowInsets: WindowInsetsControllerCompat? = null

    /**
     * ● ImageVM
     *
     * ● 2023-07-02 21:49:02 周日 下午
     */
    private val mImageVM: ImageViewModel by viewModel()

    /**
     * ● Reuqest Permission
     *
     * ● 2024-01-08 22:44:50 周一 下午
     * @author crowforkotlin
     */
    private val mActivitResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            saveBitmapToDCIM()
        } else {
            toast(getString(R.string.main_permission_tips))
            if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) || !shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { it.data = Uri.parse("package:${app.packageName}") })
            }
        }
    }

    /**
     * ● 进度加载工厂
     *
     * ● 2024-01-08 22:45:36 周一 下午
     * @author crowforkotlin
     */
    private var mProgressFactory : AppProgressFactory? = null

    /**
     * ● 返回界面
     *
     * ● 2024-01-08 22:45:56 周一 下午
     * @author crowforkotlin
     */
    private fun navigateUp() = parentFragmentManager.popSyncWithClear(Fragments.Image.name)

    /**
     * ● 保存图片至DCIM
     *
     * ● 2024-01-08 22:46:05 周一 下午
     * @author crowforkotlin
     */
    private fun saveBitmapToDCIM() {
        viewLifecycleOwner.lifecycleScope.launch {
            mImageVM.saveBitmapToDCIM((mBinding.photoview.drawable ?: return@launch).toBitmap(), mImageName!!).collect { state ->
                state.second
                    .doOnLoading { showLoadingAnim() }
                    .doOnResult {
                        dismissLoadingAnim {
                            toast("图片已保存至：${state.first}", false)
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

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.root) { view, insets, _ ->
            view.updatePadding(top = insets.top, bottom = insets.bottom )
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin= insets.right
            }
        }

        // 初始化 设置状态栏暗色
        mWindowInsets = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
        mWindowInsets?.isAppearanceLightStatusBars = false

        arguments?.let {

            mImageName = it.getString(BaseStrings.NAME)
            mImageUrl = if (AppConfig.mCoverOrinal) MangaCoilVH.getOrignalCover(it.getString(BaseStrings.IMAGE_URL) ?: return) else it.getString(BaseStrings.IMAGE_URL)
            mImageUrl.log()
        }

        /*app.imageLoader.enqueue(
            ImageRequest.Builder(mContext)
                .allowConversionToBitmap(true)
                .allowHardware(true)
                .data(mImageUrl)
                .target(mBinding.photoview)
                .build()
        )*/

        mImageUrl?.let { url ->
            mProgressFactory = AppProgressFactory.createProgressListener(url) { _, _, percentage, _, _ -> mBinding.loadingText.text = AppProgressFactory.formateProgress(percentage) }
            app.imageLoader.enqueue(
                ImageRequest.Builder(mContext)
                    .listener(
                        onSuccess = { _, _ ->
                            mBinding.loading.isInvisible = true
                            mBinding.loadingText.isInvisible = true
                        },
                        onError = { _, _ -> mBinding.loadingText.text = "-1%" },
                    )
                    .data(url)
                    .target(mBinding.photoview)
                    .allowHardware(false)
                    .allowConversionToBitmap(true)
                    .build()
            )
        }
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
        mProgressFactory?.apply {
            remove()
            removeProgressListener()
        }
        mProgressFactory = null

        // 恢复状态栏亮色 同时 置空（防止泄漏...?）
        mWindowInsets?.isAppearanceLightStatusBars = true
        mWindowInsets = null
    }
}