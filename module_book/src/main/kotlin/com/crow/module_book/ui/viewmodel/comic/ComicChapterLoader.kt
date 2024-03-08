package com.crow.module_book.ui.viewmodel.comic

import com.crow.base.tools.extensions.log
import com.crow.mangax.copymanga.BaseStrings
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_page.Content

/**
 * ● ComicChapterLoader
 *
 * ● 2024/3/9 00:17
 * @author crowforkotlin
 * @formatter:on
 */
class ComicChapterLoader {

    companion object {
        const val MAX_PAGE_SIZE = 150
        const val MAX_CHAPTER_SIZE = 2
    }

    private var mIncrementPageID: Int = 0

    /**
     * ⦁ 章节页面ID 对应 当前阅读器的内容
     *
     * ⦁ 2024-01-15 23:46:34 周一 下午
     * @author crowforkotlin
     */
    val _mChapterPageMapper: HashMap<Int, ReaderContent> = hashMapOf()
    var mPageTotalSize = 0

    /**
     * ⦁ 获取ReaderContent、根据条件决定是否删除缓存
     *
     * ⦁ 2024-03-09 01:19:59 周六 上午
     * @author crowforkotlin
     */
    fun obtainReaderContent(isNext: Boolean?, page: ComicPageResp) : ReaderContent {
        return with(page) {
            val pages = createChapterPages()
            val reader = ReaderContent(
                mComicName = mComic.mName,
                mComicUuid = mComic.mPathWord,
                mComicPathword = mComic.mPathWord,
                mPages = pages,
                mChapterInfo =  ReaderInfo (
                    mChapterIndex = mChapter.mIndex,
                    mChapterUuid = mChapter.mUuid,
                    mChapterName = mChapter.mName,
                    mChapterCount = mChapter.mCount,
                    mChapterUpdate = mChapter.mDatetimeCreated,
                    mPrevUUID = mChapter.mPrev,
                    mNextUUID = mChapter.mNext
                )
            )
            mPageTotalSize += pages.size
            if (_mChapterPageMapper.size > MAX_CHAPTER_SIZE && mPageTotalSize > MAX_PAGE_SIZE) { _mChapterPageMapper.tryDeleteCache(isNext) }
            _mChapterPageMapper[mIncrementPageID] = reader
            mIncrementPageID ++
            reader
        }
    }

    /**
     * ⦁ 尝试删除ChapterPageMapper缓存
     *
     * ⦁ 2024-03-09 01:17:16 周六 上午
     * @author crowforkotlin
     */
    private fun HashMap<Int, ReaderContent>.tryDeleteCache(isNext: Boolean?) {
        val entries = entries
        val prev = entries.first()
        val next = entries.last()
        if (isNext == true) {
            mPageTotalSize -= prev.value.mPages.size
            remove(prev.key)
        } else {
            mPageTotalSize -= next.value.mPages.size
            remove(next.key)
        }
    }

    private fun ComicPageResp.createChapterPages(): List<Content> {
        if (mChapter.mContents.isEmpty()) {
            mChapter.mWords = mutableListOf(0)
            mChapter.mContents = mutableListOf(Content(mImageUrl = BaseStrings.Repository.IMAGE_ERROR))
        }
        val pages: List<Content> = mChapter.mWords
            .zip(mChapter.mContents)
            .sortedBy { it.first }
            .mapIndexed { index, pair ->
                val content = pair.second
                content.mChapterPagePos = index
                content.mChapterID = mIncrementPageID
                content
            }
            .also { mChapter.mContents = it.toMutableList() }
            .toList()
        return pages
    }
}