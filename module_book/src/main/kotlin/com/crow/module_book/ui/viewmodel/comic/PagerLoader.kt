package com.crow.module_book.ui.viewmodel.comic

import android.content.Context
import com.crow.module_book.R
import com.crow.base.R as baseR
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderPageLoading
import com.crow.module_book.model.resp.comic_page.Content

/**
 * ● StandardLoader
 *
 * ● 2024/3/9 00:47
 * @author crowforkotlin
 * @formatter:on
 */
object PagerLoader {

    fun obtainPagerPages(context: Context, readerMappers: List<Pair<Int, ReaderContent>>) : MutableList<Any> {
        val pages: MutableList<Any> = mutableListOf()
        val noNextChapter = context.getString(R.string.book_no_next)
        val noPrevChapter = context.getString(R.string.book_no_prev)
        val loading = context.getString(baseR.string.base_loading)
        var prevLoading: ReaderPageLoading? = null
        readerMappers.forEach { pair ->
            val key = pair.first
            val value = pair.second
            val info = value.mChapterInfo
            val nextChapter = context.getString(R.string.book_next_val, info.mChapterName)
            val prevChapter = context.getString(R.string.book_prev_val, info.mChapterName)
            val prev = info.mPrevUUID
            val next = info.mNextUUID
            val current = info.mChapterUuid
            val loadingStartPos = 1
            val page = value.mPages
            val _next: String = if (next == null) { noNextChapter } else { loading }
            if (pages.isEmpty()) {
                val _prev: String = if (prev == null) { noPrevChapter } else { loading }
                val pageLoading = ReaderPageLoading(key, loadingStartPos, prevChapter, _next, prev, next, current)
                pages.add(ReaderPageLoading(key, loadingStartPos, _prev, nextChapter, prev, next, current))
                pages.addAll(page)
                pages.add(pageLoading)
                prevLoading = pageLoading
            } else {
                val pageLoading = ReaderPageLoading(key, loadingStartPos, prevChapter, _next, prev, next, current)
                pages.removeLast()
                pages.add(prevLoading!!.copy(mChapterID = key, mNextMessage = nextChapter))
                pages.addAll(page)
                pages.add(pageLoading)
                prevLoading = pageLoading
            }
        }
        return pages
//        return readerMappers.flatMap { it.second.mPages }
    }

    fun obtainErrorPages(pages: MutableList<Any>, isNext: Boolean?): MutableList<Any>? {
        if (pages.isEmpty()) return null
        if (isNext == true) {
            val last = pages.last()
            if (last is ReaderPageLoading) {
                pages.removeLast()
                pages.add(last.copy(mNextMessage = null, mLoadNext = true))
            }
        } else {
            val first = pages.first()
            if (first is ReaderPageLoading) {
                pages.removeFirst()
                pages.add(0, first.copy(mPrevMessage = null, mLoadNext = false))
            }
        }
        return pages
    }
}