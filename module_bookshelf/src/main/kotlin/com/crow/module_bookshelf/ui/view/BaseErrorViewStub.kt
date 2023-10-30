package com.crow.module_bookshelf.ui.view

import android.view.View
import android.view.ViewStub
import androidx.lifecycle.Lifecycle
import com.crow.base.ui.view.BaseViewStub
import com.crow.base.ui.view.IBaseViewStub
import com.crow.module_bookshelf.databinding.BookshelfLottieLayoutBinding

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.base.ui.view
 * @Time: 2023/10/29 21:02
 * @Author: CrowForKotlin
 * @Description: BaseViewStub
 * @formatter:on
 **************************/
class BookshelfViewStub(
    viewStub: ViewStub,
    lifecycle: Lifecycle,
    mBinding: IBaseViewStub<BookshelfLottieLayoutBinding>
) : BaseViewStub<BookshelfLottieLayoutBinding>(
    mViewStub = viewStub,
    mLifecycle = lifecycle,
    mOnBinding = mBinding
) {
    override fun bindViewBinding(view: View): BookshelfLottieLayoutBinding { return BookshelfLottieLayoutBinding.bind(view) }
}

fun bookshelfViewStub(viewStub: ViewStub, lifecyle: Lifecycle, onLayout: (BookshelfLottieLayoutBinding) -> Unit): BookshelfViewStub {
    return BookshelfViewStub(
        viewStub =  viewStub,
        lifecycle = lifecyle,
        mBinding = { _, binding ->onLayout(binding) }
    )
}
