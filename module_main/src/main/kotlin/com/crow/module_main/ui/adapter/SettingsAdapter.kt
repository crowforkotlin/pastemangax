package com.crow.module_main.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.crow.base.app.app
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.setOnCheckedInterval
import com.crow.module_main.databinding.MainFragmentSettingsSwitchRvBinding
import com.crow.module_main.model.entity.SettingContentEntity
import com.crow.module_main.model.entity.SettingSwitchEntity
import com.crow.module_main.model.entity.SettingTitleEntity
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.materialswitch.MaterialSwitch
import com.crow.base.R as baseR
import com.crow.module_home.R as homeR

class SettingsAdapter(
    private val onClick: (pos: Int) -> Unit,
    private val onChecked: (pos: Int, switch: MaterialSwitch) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    companion object {
        private const val TITLE = 0
        private const val CONTENT = 1
        private const val BUTTON = 2
    }

    data class TitleLayout(
        val mView: View,
        val mText: TextView,
        val mDiver: MaterialDivider
    )

    data class ContentLayout(
        val mView: View,
        val mImage: ImageView,
        val mText: TextView
    )

    data class SwitchLayout(
        val mView: View,
        val mImage: ImageView,
        val mSwitch: MaterialSwitch
    )

    inner class TitleVH(val mLayout: TitleLayout) : ViewHolder(mLayout.mView) {
        fun onBind(item: SettingTitleEntity) {
            mLayout.mText.text = item.mTitle
        }
    }

    inner class ContentVH(val mLayout: ContentLayout) : ViewHolder(mLayout.mView) {

        init {
            itemView.doOnClickInterval {
                onClick((getItem(absoluteAdapterPosition) as SettingContentEntity).mID)
            }
        }


        fun onBind(item: SettingContentEntity) {
            mLayout.mText.text = item.mContent
            mLayout.mImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, item.mResource ?: return run { mLayout.mImage.isGone = true }))
        }
    }

    inner class SwitchVH(val mLayout: SwitchLayout) : ViewHolder(mLayout.mView) {

        init {
            mLayout.mSwitch.setOnCheckedInterval {
                onChecked((getItem(absoluteAdapterPosition) as SettingSwitchEntity).mID, it.mType)
            }
        }

        fun onBind(item: SettingSwitchEntity) {
            mLayout.mSwitch.isChecked = item.mEnable
            mLayout.mSwitch.text = item.mContent
            mLayout.mImage.setImageDrawable(ContextCompat.getDrawable(itemView.context, item.mResource ?: return run { mLayout.mImage.isGone = true }))
        }
    }

    private val mDiffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when {
                oldItem is SettingTitleEntity && newItem is SettingTitleEntity -> oldItem.mID == newItem.mID
                oldItem is SettingContentEntity && newItem is SettingContentEntity -> oldItem.mID == newItem.mID
                oldItem is SettingSwitchEntity && newItem is SettingSwitchEntity -> oldItem.mID == newItem.mID
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return true
        }
    }

    private val mDiffer = AsyncListDiffer(this, mDiffCallback)

    private val mDp5 = app.resources.getDimensionPixelOffset(baseR.dimen.base_dp5)
    private val mDp10 = app.resources.getDimensionPixelOffset(baseR.dimen.base_dp10)
    private val mDp15 = app.resources.getDimensionPixelOffset(baseR.dimen.base_dp15)
    private val mDp20 = app.resources.getDimensionPixelOffset(baseR.dimen.base_dp20)
    private val mSp12_5 = app.resources.getDimension(baseR.dimen.base_sp12_5)
    private val mSp14 = app.resources.getDimension(baseR.dimen.base_sp14)
    private val mGrey400 = ContextCompat.getColor(app, homeR.color.home_grey_400)


    override fun getItemCount(): Int = mDiffer.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when(mDiffer.currentList[position]) {
            is SettingTitleEntity -> TITLE
            is SettingContentEntity -> CONTENT
            is SettingSwitchEntity -> BUTTON
            else -> error("parse unknow item type!")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            TITLE -> TitleVH(createTitle(parent.context))
            CONTENT -> ContentVH(createContent(parent.context))
            BUTTON -> SwitchVH(createSwitch(parent.context))
            else -> error("parse unknow item type!")
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        when(vh) {
            is TitleVH -> vh.onBind(getItem(position) as SettingTitleEntity)
            is ContentVH -> vh.onBind(getItem(position) as SettingContentEntity)
            is SwitchVH -> vh.onBind(getItem(position) as SettingSwitchEntity)
        }
    }

    private fun createTitle(context: Context) : TitleLayout {
        val layoutParams: LayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val linearLayout = LinearLayout(context)
        val textView = TextView(context)
        val diver = MaterialDivider(context)

        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(mDp15, mDp5, mDp15, mDp5)

        textView.layoutParams = layoutParams
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.maxLines = 2
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSp12_5)
        textView.setPadding(mDp5, mDp10, 0, 0)
        textView.setTextColor(mGrey400)

        diver.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).also { it.topMargin = mDp10 }
        diver.alpha = 0.5f

        linearLayout.addView(textView)
        linearLayout.addView(diver)

        return TitleLayout(
            mView = linearLayout,
            mText = textView,
            mDiver = diver
        )
    }

    private fun createContent(context: Context) : ContentLayout {
        val layoutParams: LayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val linearLayout = LinearLayout(context)
        val textView = TextView(context)
        val imageView = ImageView(context)

        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setBackgroundResource(TypedValue().apply { app.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true) }.resourceId)
        linearLayout.setPadding(mDp15, mDp15, mDp15, mDp15)

        imageView.layoutParams = LinearLayout.LayoutParams(mDp20, mDp20).also {
            it.marginEnd = mDp20
            it.gravity = Gravity.CENTER
        }
        imageView.imageTintList = ContextCompat.getColorStateList(context, baseR.color.base_color_asc)

        textView.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also {
            it.marginEnd = mDp5
            it.gravity = Gravity.CENTER
        }
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.maxLines = 1
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSp12_5)
        textView.setTextColor(ContextCompat.getColor(context, baseR.color.base_color_asc))

        linearLayout.addView(imageView)
        linearLayout.addView(textView)

        return ContentLayout(
            mView = linearLayout,
            mImage = imageView,
            mText = textView
        )
    }

    private fun createSwitch(context: Context) : SwitchLayout {
        val layoutParams: LayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val linearLayout = LinearLayout(context)
        val imageView = ImageView(context)
        val switch = MaterialSwitch(context)

        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setPadding(mDp15, mDp15, mDp15, mDp15)

        imageView.layoutParams = LinearLayout.LayoutParams(mDp20, mDp20).also {
            it.marginEnd = mDp20
            it.gravity = Gravity.CENTER
        }
        imageView.imageTintList = ContextCompat.getColorStateList(context, baseR.color.base_color_asc)

        switch.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).also {
            it.weight = 1f
            it.gravity = Gravity.CENTER
        }
        switch.typeface = Typeface.DEFAULT_BOLD
        switch.maxLines = 1
        switch.ellipsize = TextUtils.TruncateAt.END
        switch.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSp12_5)
        switch.setTextColor(ContextCompat.getColor(context, baseR.color.base_color_asc))

        linearLayout.addView(imageView)
        linearLayout.addView(switch)

        return SwitchLayout(
            mView = linearLayout,
            mImage = imageView,
            mSwitch = switch
        )
    }
    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(contents: MutableList<Any>) = mDiffer.submitList(contents)
}