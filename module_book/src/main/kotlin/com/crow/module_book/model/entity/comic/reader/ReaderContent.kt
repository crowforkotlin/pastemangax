package com.crow.module_book.model.entity.comic.reader


/**
 * Reader content
 *
 * @property mComicName 漫画名称
 * @property mComicUUID 漫画UUID
 * @property mComicPathword 漫画路径
 * @property mPages 页面列表
 * @property mChapterInfo 章节信息
 * @constructor Create empty Reader content
 */
data class ReaderContent(
    val mComicName: String,
    val mComicUUID: String,
    val mComicPathword: String,
    val mPages: List<Any>,
    val mChapterInfo: ReaderInfo?
)
