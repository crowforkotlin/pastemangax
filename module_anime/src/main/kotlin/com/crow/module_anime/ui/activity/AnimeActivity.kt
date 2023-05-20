package com.crow.module_anime.ui.activity

import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.module_anime.databinding.AnimeActivityBinding

class AnimeActivity : BaseMviActivity<AnimeActivityBinding>() {

    private val mPlayer by lazy { ExoPlayer.Builder(this).build() }

    override fun getViewBinding() = AnimeActivityBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {}
}