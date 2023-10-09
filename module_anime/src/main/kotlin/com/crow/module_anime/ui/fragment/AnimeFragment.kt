package com.crow.module_anime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.media3.common.util.UnstableApi
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.module_anime.databinding.AnimeFragmentBinding
import com.crow.module_anime.ui.viewmodel.AnimeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
    private val mPlayer by lazy { ExoPlayer.Builder(mContext).build() }

    // 创建一个SimpleExoPlayer并设置到PlayerView
    mBinding.playerView.player = mPlayer
    mBinding.playerView.setControllerOnFullScreenModeChangedListener {

    }
    // 构建MediaItem
    val uri = Uri.parse("https://vod77.fgjfghkkconsulting.xyz/videos/fb521050d6fe71edbfe197c6360c0102/hls/1080p/main.m3u8")
    val mediaItem = MediaItem.fromUri(uri)


    // 设置MediaItem到播放器并准备播放
    mPlayer.setMediaItem(mediaItem)
    mPlayer.prepare()

* * */

@UnstableApi
class AnimeFragment : BaseMviFragment<AnimeFragmentBinding>() {

    private val mVM by lazy { viewModel<AnimeViewModel>()  }

    override fun getViewBinding(inflater: LayoutInflater) = AnimeFragmentBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

    }
}