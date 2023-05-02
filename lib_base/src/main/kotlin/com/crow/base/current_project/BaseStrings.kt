package com.crow.base.current_project

object BaseStrings {

    const val PATH_WORD = "pathword"


    object URL {

        fun setCopyMangaUrl(tld: String) {
            CopyManga = "https://api.copymanga$tld"
        }

        // (Top-Level Domain，TLD)
        const val CopyManga_TLD_TV = ".tv"
        const val CopyManga_TLD_SITE = ".site"

        const val Crow_Site = "https://gitee.com/llzzppFlash/copy-manga/raw/main/site.json"
        const val Crow_UpdateInfo = "https://gitee.com/llzzppFlash/copy-manga/raw/main/update_info.json"
        const val Crow_QQGroup = "https://gitee.com/llzzppFlash/copy-manga/raw/main/qq_group"

        const val MangaFuna = "https://hi77-overseas.mangafuna.xyz/"
        var CopyManga = "https://api.copymanga$CopyManga_TLD_TV"
        const val Login = "/api/v3/login"
        const val Reg = "/api/v3/register"

        const val ComicInfo = "/api/v3/comic2/{pathword}?platform=1&_update=true"
        const val ComicChapter = "/api/v3/comic/{pathword}/group/default/chapters?_update=true"
        const val ComicPage = "/api/v3/comic/{pathword}/chapter2/{uuid}?platform=3&_update=true"
        const val ComicBrowserHistory = "/api/v3/comic2/{pathword}/query?platform=1&_update=true"

        const val NovelInfo = "/api/v3/book/{pathword}?_update=true"
        const val NovelChapter = "/api/v3/book/{pathword}/volumes"
        const val NovelBrowserHistory = "/api/v3/book/{pathword}/query"
        const val NovelCatelogue = "/api/v3/book/{pathword}/volume/1847?_update=true"

        const val HomePage = "/api/v3/h5/homeIndex"
        const val RefreshRec = "/api/v3/recs"
        const val UserUpdateInfo = "/api/v3/member/update/info"
        const val UserInfo = "/api/v3/member/info"
        const val BookshelfComic = "/api/v3/member/collect/comics?free_type=1&_update=true"
        const val BookshelfNovel = "/api/v3/member/collect/books?free_type=1&_update=true"

        const val DiscoverComicTag = "/api/v3/h5/filter/comic/tags"
        const val DiscoverComicHome = "/api/v3/comics?free_type=1&_update=true"
        const val DiscoverNovelTag = "/api/v3/h5/filter/book/tags"
        const val DiscoverNovelHome = "/api/v3/books?free_type=1&_update=true"
    }

    object Key {
        const val POST_CURRENT_ITEM = "POST_CURRENT_ITEM"
        const val LOGIN_SUCUESS = "LOGIN_SUCUESS"
        const val CLEAR_USER_INFO = "CLEAR_USER_INFO"
        const val EXIT_USER = "EXIT_USER"
        const val SET_HOME_ICON = "SET_HOME_ICON"
        const val CHECK_UPDATE = "CHECK_UPDATE"
    }

    object FragmentTag {
        
    }
}