⭐⭐⭐**AdapterDescription**⭐⭐⭐

<br/>

- ### 🟠**Why do you need to set the default state in advance in on Bind View Holder?**
    - 🟢**ReferToThePicture**
    - ![微信图片_20230509231616](https://github.com/CrowForKotlin/CopyManga_Crow/assets/60876546/8eeff185-252d-4f5f-9c76-02b380ba6cdd)
    - 
    - 🟢**This writing code is basically the same idea in almost every module in the entire project! So you need to understand it simply**
    - 🟢**In the onBindViewHolder method, we first set default values for each ViewHolder to ensure that the loading progress is displayed correctly. Since Glide loads images asynchronously, when we process images, we need to enable the cache function of RecyclerView to prevent memory from skyrocketing. At the same time, before setting the default value, we need to cancel the previous fade-out animation to prevent the ViewHolder from being reused when the animation is not completed, causing the loading progress effect of the next reused ViewHolder to disappear. To avoid this, we call the cancel() method to cancel the animation before setting the default value. Although we set alpha to 1f to restore the default state, if the animation is not canceled, the reused view loading animation may already be in an invisible state. Therefore, we must use both the cancel() action and set the alpha value to ensure that the loading progress is displayed correctly.**
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