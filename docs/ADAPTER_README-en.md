⭐⭐⭐**AdapterDescription**⭐⭐⭐

<br/>

- ### ⚠️This does not apply to long images, such as those that are wide x high Y1000+ or even larger
- 
- ### 🟠**Why do you need to set the default state in advance in on Bind View Holder?**
    - 🟢**ReferToThePicture**
    - ![微信图片_20230509231616](https://github.com/CrowForKotlin/CopyManga_Crow/assets/60876546/8eeff185-252d-4f5f-9c76-02b380ba6cdd)
    - 
    - 🟢**This code exists in almost every module of the entire project, basically the same idea! So you need to briefly understand 0_0**
    - 🟢**In the onBindViewHolder method, we first set a default value for each ViewHolder to ensure that the loading progress is displayed correctly. Since Glide loads images asynchronously, when we process images, we need to turn on RecyclerView's caching function to prevent memory explosion. At the same time, before setting the default value, we need to make the view visible, otherwise it will cause the loading progress effect of the next reused ViewHolder to disappear. So, we have to use default visibility to ensure that the loading progress is displayed correctly.**
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