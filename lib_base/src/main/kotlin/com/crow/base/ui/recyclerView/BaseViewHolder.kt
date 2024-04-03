package com.crow.base.ui.recyclerView

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.decode.DecodeResult
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.crow.base.app.app
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.doOnClickInterval
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * ● BaseViewHolder
 *
 * ● 2024/3/28 23:02
 * @author crowforkotlin
 * @formatter:on
 */
abstract class BaseViewHolder<VB: ViewBinding>(baseBinding: VB) : RecyclerView.ViewHolder(baseBinding.root)  {

}