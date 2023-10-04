package com.crow.module_anime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.media3.exoplayer.ExoPlayer
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_anime.databinding.AnimeFragmentBinding

class AnimeFragment : BaseMviFragment<AnimeFragmentBinding>() {

    private val mPlayer by lazy { ExoPlayer.Builder(mContext).build() }

    private val mVM by lazy {  }

    override fun getViewBinding(inflater: LayoutInflater) = AnimeFragmentBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

    }
}