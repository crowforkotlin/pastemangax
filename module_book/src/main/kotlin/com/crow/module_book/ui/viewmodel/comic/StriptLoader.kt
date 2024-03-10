package com.crow.module_book.ui.viewmodel.comic

import android.content.Context
import com.crow.module_book.R
import com.crow.base.R as baseR
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderLoading

/**
 * ● StandardLoader
 *
 * ● 2024/3/9 00:47
 * @author crowforkotlin
 * @formatter:on
 */
object StriptLoader {

    fun obtaintStriptPages(context: Context, readerMappers: List<Pair<Int, ReaderContent>>) : MutableList<Any> {
        val pages: MutableList<Any> = mutableListOf()
        val noNextChapter = context.getString(R.string.book_no_next)
        val noLastChapter = context.getString(R.string.book_no_prev)
        val loading = context.getString(baseR.string.base_loading)
        val entriesLastIndex = readerMappers.size - 1
        readerMappers.forEachIndexed { index, pair ->
            val key = pair.first
            val value = pair.second
            val info = value.mChapterInfo!!
            val nextChapter = context.getString(R.string.book_next_val, info.mChapterName)
            val lastChapter = context.getString(R.string.book_prev_val, info.mChapterName)
            val prev = info.mPrevUUID
            val next = info.mNextUUID
            val current = info.mChapterUuid
            val loadingStartPos = 1
            val page = value.mPages
            val pageSize = page.size
            if (pages.isEmpty()) {
                if(prev == null) {
                    pages.add(ReaderLoading(key, loadingStartPos, noLastChapter, null, next, current))
                } else {
                    pages.add(ReaderLoading(key, loadingStartPos, loading, prev, next, current))
                }
            }
            pages.add(ReaderLoading(key, loadingStartPos, nextChapter, prev, next, current))
            pages.addAll(page)
            pages.add(ReaderLoading(key, pageSize, lastChapter, prev, next, current))
            if (entriesLastIndex == 0 || entriesLastIndex == index) {
                if (next == null) {
                    pages.add(ReaderLoading(key, pageSize, noNextChapter, prev, null, current))
                } else {
                    pages.add(ReaderLoading(key, pageSize, loading, prev, next, current))
                }
            }
        }
        return pages
    }
}