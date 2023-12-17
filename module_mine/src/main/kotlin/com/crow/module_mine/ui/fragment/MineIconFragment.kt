package com.crow.module_mine.ui.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.crow.mangax.copymanga.entity.AppConfigEntity.Companion.mDarkMode
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.popAsyncWithClear
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_mine.R
import com.crow.module_mine.databinding.MineFragmentIconBinding
import com.crow.module_mine.ui.tools.GlideEngine
import com.crow.module_mine.ui.viewmodel.MineViewModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR

class MineIconFragment : BaseMviFragment<MineFragmentIconBinding>() {

    // WindowInsets属性 （状态栏属性设置等...）
    private var mWindowInsets: WindowInsetsControllerCompat? = null

    // 共享 用户VM
    private val mUserVM by activityViewModel<MineViewModel>()

    private fun navigateUp() = parentFragmentManager.popAsyncWithClear(Fragments.Icon.name)

    override fun getViewBinding(inflater: LayoutInflater) = MineFragmentIconBinding.inflate(inflater)

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 恢复状态栏亮色 同时 置空（防止泄漏...?）
        mWindowInsets?.isAppearanceLightStatusBars = !mDarkMode
        mWindowInsets = null
    }

    override fun initObserver(saveInstanceState: Bundle?) {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 初始化 Icon链接 设置用户名
            mUserVM.doLoadIcon(mContext, false) { resource ->  mBinding.userIconPhotoview.setImageDrawable(resource) }

            // 用户信息为空 则设置编辑按钮消失
            if (it == null) mBinding.userIconEdit.visibility = View.GONE
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionPadding(mBinding.root)

        // 初始化 设置状态栏暗色
        mWindowInsets = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
        mWindowInsets?.isAppearanceLightStatusBars = false
    }

    override fun initListener() {

        // 修改头像 点击事件监听
        mBinding.userIconEdit.doOnClickInterval {

            //  创建一个MaterialDialog用于提示，同意后打开相册（支持裁剪）
            mContext.newMaterialDialog { dialog ->
                dialog.setView(TextView(dialog.context).also { textView ->
                        textView.text = mContext.getString(R.string.mine_upload_icon_tips)
                        textView.textSize = 18f
                        textView.setPadding(mContext.resources.getDimensionPixelSize(baseR.dimen.base_dp20))
                        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        textView.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                })
                dialog.setTitle(getString(R.string.mine_upload_icon_tips_title))
                dialog.setPositiveButton(R.string.mine_upload_icon_tips_agreen) { dialogs, _ ->
                    dialogs.dismiss()
                    PictureSelector.create(mContext)
                        .openGallery(SelectMimeType.ofImage())
                        .setSelectionMode(SelectModeConfig.SINGLE)
                        .setImageEngine(GlideEngine.createGlideEngine())
                        .setCropEngine { fragment, srcUri, destinationUri, dataSource, requestCode ->
                            val uCrop = UCrop.of(srcUri, destinationUri, dataSource)
                            uCrop.setImageEngine(object : UCropImageEngine {
                                override fun loadImage(context: Context?, url: Uri?, maxWidth: Int, maxHeight: Int, call: UCropImageEngine.OnCallbackListener<Bitmap>?) {}
                                override fun loadImage(context: Context?, url: String?, imageView: ImageView?) { Glide.with(context!!).load(url).into(imageView!!) }
                            })
                            uCrop.withOptions(UCrop.Options().also { it.isDarkStatusBarBlack(true) })
                            uCrop.start(fragment.requireActivity(), fragment, requestCode)
                        }
                        .forResult(object : OnResultCallbackListener<LocalMedia?> {
                            override fun onResult(result: ArrayList<LocalMedia?>?) {
                                // val cutImgFile = File(result?.get(0)?.cutPath ?: return)
                            }
                            override fun onCancel() {}
                        })
                }
            }
        }
    }
}