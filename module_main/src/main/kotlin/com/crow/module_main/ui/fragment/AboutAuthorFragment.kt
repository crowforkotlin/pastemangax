package com.crow.module_main.ui.fragment

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crow.base.current_project.getSpannableString
import com.crow.base.tools.extensions.*
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentAboutBinding
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_user/src/main/kotlin/com/crow/module_user/ui/fragment
 * @Time: 2023/4/7 13:13
 * @Author: CrowForKotlin
 * @Description: AboutAuthor
 * @formatter:on
 **************************/
class AboutAuthorFragment : BaseMviFragment<MainFragmentAboutBinding>() {

    private val mContainerVm by viewModel<ContainerViewModel>()

    companion object { fun newInstance(): AboutAuthorFragment = AboutAuthorFragment() }

    private fun navigateUp() = parentFragmentManager.popSyncWithClear("AboutAuthorFragment", "ContainerFragment")

    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentAboutBinding.inflate(inflater)

    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    override fun initView(bundle: Bundle?) {

        mBinding.root.setPadding(0, mContext.getStatusBarHeight(), 0, mContext.getNavigationBarHeight())

        Glide.with(mContext)
            .load(R.drawable.main_icon_crow)
            .apply(RequestOptions().circleCrop().override(mContext.resources.getDimensionPixelSize(baseR.dimen.base_dp36)))
            .into(object : CustomTarget<Drawable>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) { mBinding.mainAboutIcon.setImageDrawable(resource) }
            })


        val builder = SpannableStringBuilder()
        val purple = ContextCompat.getColor(mContext, baseR.color.base_purple_8f6af1)
        builder.appendLine(mContext.getString(R.string.main_about_crow).getSpannableString(purple, 5))
        builder.appendLine()
        builder.appendLine(mContext.getString(R.string.main_about_crow_email).getSpannableString(purple, 5))
        mBinding.mainAboutContent.text = builder
        mBinding.mainAboutAppVersion.text = getString(R.string.main_about_app_version, getCurrentVersionName().split("_")[0])
    }

    override fun initObserver() {
        mContainerVm.onOutput {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            when(it) {
                is ContainerIntent.GetQQGroup -> {
                    it.mViewState
                        .doOnResult {
                            intent.data = Uri.parse(it.link!!)
                            startActivity(intent)
                        }
                        .doOnError { _, _ ->
                            intent.data = Uri.parse(getString(R.string.main_about_qq_gropu))
                            startActivity(intent)
                        }
                }
            }
        }
    }

    override fun initListener() {
        mBinding.mainAboutBack.clickGap { _, _ -> navigateUp() }
        mBinding.userAboutAddQqGroup.clickGap { _, _ ->
            mContainerVm.input(ContainerIntent.GetQQGroup())
        }
    }
}