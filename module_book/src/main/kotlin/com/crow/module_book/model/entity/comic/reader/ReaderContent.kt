package com.crow.module_book.model.entity.comic.reader

import com.crow.module_book.model.resp.comic_page.Content


/**
 * Reader content
 *
 * @property mComicName 漫画名称
 * @property mComicUuid 漫画UUID
 * @property mComicPathword 漫画路径
 * @property mPages 页面列表
 * @property mChapterInfo 章节信息
 * @constructor Create empty Reader content
 */
data class ReaderContent(
    val mComicName: String,
    val mComicUuid: String,
    val mComicPathword: String,
    val mPages: List<Content>,
    val mChapterInfo: ReaderInfo
)
