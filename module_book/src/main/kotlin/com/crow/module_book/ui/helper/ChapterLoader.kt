package com.crow.module_book.ui.helper

import com.crow.base.app.app
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_page.Content
import kotlinx.coroutines.sync.Mutex

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.helper
 * @Time: 2023/8/31 23:50
 * @Author: CrowForKotlin
 * @Description: ChapterLoader
 * @formatter:on
 **************************/
class ChapterLoader {

    enum class ChapterLoaderPolicy { MEMORY, DISK }

    companion object { private var PRELOAD_SIZE = 10 }

    private var mMutex = Mutex()

    private val mPolicy = ChapterLoaderPolicy.DISK

    /**
     * ● 漫画内容
     *
     * ● 2023-06-28 22:08:26 周三 下午
     */
    var mComicList: List<Content>? = null
        private set

    suspend fun loadPreNextChapter(readerInfo: ReaderInfo, comicList: List<Content>, position: Int, isNext: Boolean) {
        when(mPolicy) {
            ChapterLoaderPolicy.MEMORY -> {}
            ChapterLoaderPolicy.DISK ->{
                loadOnDiskChapter(readerInfo, comicList, position)
            }
        }
    }

    private suspend fun loadOnDiskChapter(readerInfo: ReaderInfo, pages: List<Content>, isNext: Int) {
//        app.filesDir.
    }

    private suspend fun loadOnMemoryChapter(pages: ComicPageResp, isNext: Boolean) {
        // TODO
    }

    private fun getComicCacheFile(readerInfo: ReaderInfo) {
//       File(app.cacheDir, "comic/${readerInfo.m}")

    }
}