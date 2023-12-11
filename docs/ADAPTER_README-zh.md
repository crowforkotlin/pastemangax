⭐⭐⭐**适配器说明**⭐⭐⭐

<br/>

- ### ⚠️This does not apply to long images, such as those that are wide x high Y1000+ or even larger

- ### 🟠为什么在onBindViewHolder中需要先提前设置默认状态？
    - 🟢**参考如图**
    - ![微信图片_20230509231616](https://github.com/CrowForKotlin/CopyManga_Crow/assets/60876546/8eeff185-252d-4f5f-9c76-02b380ba6cdd)
    - 
    - 🟢**这些代码几乎在整个项目中的各个模块都存在基本上都是相同的思路！所以需要简单理解一下0_0**
    - 🟢**在 onBindViewHolder 方法中，我们首先为每个 ViewHolder 设置默认值，以确保正确显示加载进度。由于 Glide 加载图片是异步的，当我们处理图片时，需要开启 RecyclerView 的缓存功能，以防止内存暴涨。同时，在设置默认值之前，我们需要将视图设置为可见，不然会导致下一个复用的 ViewHolder 的加载进度效果消失。所以，我们必须使用默认可见来确保正确显示加载进度。**
    ```kotlin
  override fun onBindViewHolder(vh: LoadingViewHolder, position: Int) {
        val item = getItem(position) ?: return

        vh.rvBinding.bookshelfRvLoading.isVisible = true
        vh.rvBinding.bookshelfRvProgressText.isVisible = true
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
                    vh.rvBinding.isInvisilibity = true
                    vh.rvBinding.isInvisilibity = true
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    vh.rvBinding.bookshelfRvLoading.isInvisilibity = true
                    vh.rvBinding.bookshelfRvProgressText.isInvisilibity = true
                    NoTransition()
                }
            })
            .into(vh.rvBinding.bookshelfRvImage)
        vh.rvBinding.bookshelfRvName.text = item.mComic.mName
        vh.rvBinding.bookshelfRvTime.text = item.mComic.mDatetimeUpdated
  }
    ```