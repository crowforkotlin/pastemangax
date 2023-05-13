⭐⭐⭐**AdapterDescription**⭐⭐⭐

<br/>

- ### 🟠Why do you need to set the default state in advance in on Bind View Holder?
    - 🟢ReferToThePicture
    - ![微信图片_20230509231616](https://github.com/CrowForKotlin/CopyManga_Crow/assets/60876546/8eeff185-252d-4f5f-9c76-02b380ba6cdd)
    - **This writing code is basically the same idea in almost every module in the entire project! So you need to understand it simply**
    - **In the onBindViewHolder method, we first set default values for each ViewHolder to ensure that the loading progress is displayed correctly. Since Glide loads images asynchronously, when we process images, we need to enable the cache function of RecyclerView to prevent memory from skyrocketing. At the same time, before setting the default value, we need to cancel the previous fade-out animation to prevent the ViewHolder from being reused when the animation is not completed, causing the loading progress effect of the next reused ViewHolder to disappear. To avoid this, we call the cancel() method to cancel the animation before setting the default value. Although we set alpha to 1f to restore the default state, if the animation is not canceled, the reused view loading animation may already be in an invisible state. Therefore, we must use both the cancel() action and set the alpha value to ensure that the loading progress is displayed correctly.**