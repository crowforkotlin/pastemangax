package com.crow.module_bookshelf

import android.view.LayoutInflater
import com.crow.base.fragment.BaseVBFragment
import com.crow.module_bookshelf.databinding.BookshelfFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookShelfFragment : BaseVBFragment<BookshelfFragmentBinding, BookShelfViewModel>() {

    override fun getViewBinding(inflater: LayoutInflater) = BookshelfFragmentBinding.inflate(inflater)

    override fun getViewModel(): Lazy<BookShelfViewModel> = viewModel()

}