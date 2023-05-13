⭐⭐⭐**适配器说明**⭐⭐⭐

<br/>

- ### 🟠为什么在onBindViewHolder中需要先提前设置默认状态？
    - 🟢**参考如图**
    - ![微信图片_20230509231616](https://github.com/CrowForKotlin/CopyManga_Crow/assets/60876546/8eeff185-252d-4f5f-9c76-02b380ba6cdd)
    - 
    - 🟢**这写代码几乎在整个项目中的各个模块都存在基本上都是相同的思路！所以需要简单理解一下**
    - 🟢**在 onBindViewHolder 方法中，我们首先为每个 ViewHolder 设置默认值，以确保正确显示加载进度。由于 Glide 加载图片是异步的，当我们处理图片时，需要开启 RecyclerView 的缓存功能，以防止内存暴涨。同时，在设置默认值之前，我们需要取消之前的淡出动画，以防止动画未执行完毕时 ViewHolder 被复用，导致下一个复用的 ViewHolder 的加载进度效果消失。为了避免这种情况，我们在设置默认值之前调用了 cancel() 方法来取消动画。虽然我们设置了 alpha 为 1f 来恢复默认状态，但如果不取消动画，复用的视图加载动画可能已经处于看不见的状态。因此，我们必须同时使用 cancel() 操作和设置 alpha 值来确保正确显示加载进度。**
    ```kotlin
  override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.mLoadingPropertyAnimator?.cancel()
        vh.mTextPropertyAnimator?.cancel()
        vh.mLoadingPropertyAnimator = null
        vh.mTextPropertyAnimator = null
        vh.rvBinding.bookshelfRvLoading.alpha = 1f
        vh.rvBinding.bookshelfRvProgressText.alpha = 1f
        vh.rvBinding.bookshelfRvProgressText.text = AppGlideProgressFactory.PERCENT_0
        vh.mAppGlideProgressFactory?.doRemoveListener()?.doClean()
        vh.mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(item.mComic.mCover) { _, _, percentage, _, _ ->
            vh.rvBinding.bookshelfRvProgressText.text = AppGlideProgressFactory.getProgressString(percentage)
        }

        Glide.with(vh.itemView.context)
            .load(item.mComic.mCover)
            .listener(vh.mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    vh.mLoadingPropertyAnimator = vh.rvBinding.bookshelfRvLoading.animateFadeOut()
                    vh.mTextPropertyAnimator = vh.rvBinding.bookshelfRvProgressText.animateFadeOut()
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.rvBinding.bookshelfRvLoading.alpha = 0f
                    vh.rvBinding.bookshelfRvProgressText.alpha = 0f
                    NoTransition()
                }
            })
            .into(vh.rvBinding.bookshelfRvImage)
        vh.rvBinding.bookshelfRvName.text = item.mComic.mName
        vh.rvBinding.bookshelfRvTime.text = item.mComic.mDatetimeUpdated
  }
    ```