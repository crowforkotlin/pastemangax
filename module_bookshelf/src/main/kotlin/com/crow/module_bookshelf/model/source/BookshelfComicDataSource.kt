package com.crow.module_bookshelf.model.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.crow.module_bookshelf.model.resp.BookshelfComicResp
import com.crow.module_bookshelf.model.resp.bookshelf_comic.BookshelfComicResults

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_bookshelf/src/main/kotlin/com/crow/module_bookshelf/model/source
 * @Time: 2023/3/23 0:42
 * @Author: CrowForKotlin
 * @Description: BookShelfDataSource
 * @formatter:on
 **************************/
class BookshelfComicDataSource(inline val mDoOnPageResults: suspend (position: Int, pageSize: Int) -> BookshelfComicResp?) : PagingSource<Int, BookshelfComicResults>() {

    companion object {
        private const val START_POSITION = 0
        private const val LOAD_POSITION = 20
    }

    // 当刷新时调用
    override fun getRefreshKey(state: PagingState<Int, BookshelfComicResults>): Int? {

        // 获取最近的结尾页面位置
        val anchorPage = state.closestPageToPosition(state.anchorPosition ?: return null)

        // 刷新后重载的Load函数返回值会让nextKey + 20 此时判断nextKey 不为空则 -20 即 position为 + 20 - 20 原封不
        return anchorPage?.prevKey?.plus(LOAD_POSITION) ?: anchorPage?.nextKey?.minus(LOAD_POSITION)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BookshelfComicResults> {

        // 当前位置
        val position = params.key ?: START_POSITION

        // 下一个位置
        val nextPos = position + LOAD_POSITION


        return try {

            // 获取书架漫画结果集
            val result = mDoOnPageResults(position, params.loadSize) ?: return LoadResult.Page(data = mutableListOf(), null, null)

            // 下一个键 = 如果 当前位置 + 预加载页数 大于 总共的数量则返回空 否则 返回下一个起点
            val nextKey = if (nextPos > result.mTotal) null else nextPos

            // 返回数据
            mutableListOf<BookshelfComicResults>().run {
                addAll(result.mList)
                if (isEmpty()) LoadResult.Page(this, prevKey = null, nextKey = null)
                else LoadResult.Page(this, prevKey = null, nextKey = nextKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}