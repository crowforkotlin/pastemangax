package com.crow.module_book.model.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.crow.module_book.model.resp.ComicCommentListResp
import com.crow.module_book.model.resp.comic_comment.ComicCommentListResult

class ComicCommentDataSource(inline val onResult: suspend (position: Int, pageSize: Int) -> ComicCommentListResp?) : PagingSource<Int, ComicCommentListResult>() {

    companion object {
        private const val START_POSITION = 0
        private const val LOAD_POSITION = 20
    }

    // 当刷新时调用
    override fun getRefreshKey(state: PagingState<Int, ComicCommentListResult>): Int? {

        // 获取最近的结尾页面位置
        val anchorPage = state.closestPageToPosition(state.anchorPosition ?: return null)

        // 刷新后重载的Load函数返回值会让nextKey + 20 此时判断nextKey 不为空则 -20 即 position为 + 20 - 20 原封不
        return anchorPage?.prevKey?.plus(LOAD_POSITION) ?: anchorPage?.nextKey?.minus(LOAD_POSITION)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ComicCommentListResult> {

        // 当前Position
        val position = params.key ?: START_POSITION

        // 下一个位置
        val nextPos = position + LOAD_POSITION

        return try {

            // 获取漫画章节结果集
            val result = onResult(position, params.loadSize) ?: return LoadResult.Page(mutableListOf(), null, null)

            // 下一个键 = 如果nextPos 大于 总数 为null 否则 下一个位置
            val nextKey = if (nextPos > (result.mTotal ?: 0)) null else nextPos

            // 返回结果
            mutableListOf<ComicCommentListResult>().run {
                addAll(result.mList ?: listOf())
                if (isEmpty()) LoadResult.Page(this, prevKey = null, nextKey = null)
                else LoadResult.Page(this, prevKey = null, nextKey = nextKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
                   LoadResult.Error(e)
        }
    }
}