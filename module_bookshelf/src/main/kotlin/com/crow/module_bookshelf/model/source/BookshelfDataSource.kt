package com.crow.module_bookshelf.model.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.crow.base.tools.extensions.logMsg
import com.crow.module_bookshelf.model.resp.book_shelf.BookshelfResults

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_bookshelf/src/main/kotlin/com/crow/module_bookshelf/model/source
 * @Time: 2023/3/23 0:42
 * @Author: CrowForKotlin
 * @Description: BookShelfDataSource
 * @formatter:on
 **************************/
class BookshelfDataSource(inline val mDoOnPageResults: suspend (position: Int, pageSize: Int) -> List<BookshelfResults>?) : PagingSource<Int, BookshelfResults>() {

    companion object {
        private const val START_POSITION = 0
        private const val LOAD_POSITION = 20
    }

    // 当刷新时调用
    override fun getRefreshKey(state: PagingState<Int, BookshelfResults>): Int? {

        // 获取最近的结尾页面位置
        val anchorPage = state.closestPageToPosition(state.anchorPosition ?: return null)

        "prevKey : ${anchorPage?.prevKey}\tnextKey : ${anchorPage?.nextKey}".logMsg()

        // 刷新后重载的Load函数返回值会让nextKey + 20 此时判断nextKey 不为空则 -20 即 position为 + 20 - 20 原封不
        return anchorPage?.prevKey?.plus(LOAD_POSITION) ?: anchorPage?.nextKey?.minus(LOAD_POSITION)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BookshelfResults> {
        val position = params.key ?: START_POSITION
        "position : $position\tpageSize ${params.loadSize}".logMsg()
        return try {
            val realDataList = mutableListOf<BookshelfResults>().apply { addAll(mDoOnPageResults(position, params.loadSize) ?: return@apply) }
            if (realDataList.isEmpty()) LoadResult.Page(data = realDataList, prevKey = null, nextKey = null)
            else LoadResult.Page(data = realDataList, prevKey = null, nextKey = position + LOAD_POSITION)
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}