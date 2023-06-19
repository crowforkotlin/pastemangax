package com.crow.module_discover.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import com.crow.base.ui.fragment.BaseMviBottomSheetDialogFragment
import com.crow.module_discover.databinding.DiscoverFragmentBottomComicBinding

class ComicBottomFragment : BaseMviBottomSheetDialogFragment<DiscoverFragmentBottomComicBinding>() {

    override fun getViewBinding(inflater: LayoutInflater) = DiscoverFragmentBottomComicBinding.inflate(layoutInflater)

    override fun initView(bundle: Bundle?) {

    }
}