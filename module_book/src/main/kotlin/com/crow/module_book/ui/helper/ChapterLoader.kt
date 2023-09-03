package com.crow.module_book.ui.helper

import com.crow.module_book.model.resp.ComicPageResp
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

    var mutex = Mutex()

    private val mPolicy = ChapterLoaderPolicy.MEMORY

    /**
     * ● 漫画内容
     *
     * ● 2023-06-28 22:08:26 周三 下午
     */
    var mComicPage: ComicPageResp? = null
        private set



    suspend fun loadPreNextChapter(pages: ComicPageResp, isNext: Boolean) {
        when(mPolicy) {
            ChapterLoaderPolicy.MEMORY -> loadOnMemoryChapter(pages, isNext)
            ChapterLoaderPolicy.DISK ->{

            }
        }
    }

    private suspend fun loadOnMemoryChapter(pages: ComicPageResp, isNext: Boolean) {
        if (isNext) {

        } else {

        }
    }
}