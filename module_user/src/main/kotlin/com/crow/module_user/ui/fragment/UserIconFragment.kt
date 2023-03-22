package com.crow.module_user.ui.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_user.R
import com.crow.module_user.databinding.UserFragmentIconBinding
import com.crow.module_user.ui.tools.GlideEngine
import com.crow.module_user.ui.viewmodel.UserViewModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File
import com.crow.base.R as baseR


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/3/21 2:14
 * @Author: CrowForKotlin
 * @Description: UserIconFragment
 * @formatter:on
 **************************/

class UserIconFragment : BaseMviFragment<UserFragmentIconBinding>() {

    // WindowInsets属性 （状态栏属性设置等...）
    private var mWindowInsets: WindowInsetsControllerCompat? = null

    // 共享 用户VM
    private val mUserVM by sharedViewModel<UserViewModel>()

    override fun getViewBinding(inflater: LayoutInflater) = UserFragmentIconBinding.inflate(inflater)

    override fun onDestroyView() {
        super.onDestroyView()

        // 恢复状态栏亮色 同时 置空（防止泄漏...?）
        mWindowInsets?.isAppearanceLightStatusBars = true
        mWindowInsets = null
    }

    override fun initObserver() {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 初始化 Icon链接 设置用户名
            mUserVM.doLoadIcon(mContext, false) { resource ->  mBinding.userIconPhotoview.setImageDrawable(resource) }

            // 用户信息为空 则设置编辑按钮消失
            if (it == null) mBinding.userIconEdit.visibility = View.GONE
        }
    }

    override fun initView() {

        // 设置 内边距属性 实现沉浸式效果
        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())

        // 初始化 设置状态栏暗色
        mWindowInsets = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
        mWindowInsets?.isAppearanceLightStatusBars = false
    }

    override fun initListener() {

        // 修改头像 点击事件监听
        mBinding.userIconEdit.clickGap { _, _ ->

            //  创建一个MaterialDialog用于提示，同意后打开相册（支持裁剪）
            mContext.newMaterialDialog { dialog ->
                dialog.setView(TextView(dialog.context).also { textView ->
                        textView.text = mContext.getString(R.string.user_upload_icon_tips)
                        textView.textSize = 18f
                        textView.setPadding(mContext.resources.getDimensionPixelSize(baseR.dimen.base_dp20))
                        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        textView.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                })
                dialog.setTitle(getString(R.string.user_upload_icon_tips_title))
                dialog.setPositiveButton(R.string.user_upload_icon_tips_agreen) { dialogs, _ ->
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
                                val cutImgFile = File(result?.get(0)?.cutPath ?: return)
                                cutImgFile.logMsg()
                            }
                            override fun onCancel() {}
                        })
                }
            }
        }
    }
}