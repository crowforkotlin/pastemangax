package com.crow.mangax.tools.language

/**
 * Created by zhangqichuan on 2/3/16.
 */
enum class ConversionType {
    HK2S,

    //hk2s.json Traditional Chinese (Hong Kong Standard) to Simplified Chinese 香港繁體（香港小學學習字詞表標準）到簡體
    HK2T,

    //hk2t.json Traditional Chinese (Hong Kong variant) to Traditional Chinese 香港繁體（香港小學學習字詞表標準）到繁體
    JP2T,

    //jp2t.json New Japanese Kanji (Shinjitai) to Traditional Chinese Characters (Kyūjitai) 日本漢字到繁體
    S2HK,

    //s2hk.json Simplified Chinese to Traditional Chinese (Hong Kong Standard) 簡體到香港繁體（香港小學學習字詞表標準）
    S2T,

    //s2t.json Simplified Chinese to Traditional Chinese 簡體到繁體
    S2TW,

    //s2tw.json Simplified Chinese to Traditional Chinese (Taiwan Standard) 簡體到臺灣正體
    S2TWP,

    //s2twp.json Simplified Chinese to Traditional Chinese (Taiwan Standard) with Taiwanese idiom 簡體到繁體（臺灣正體標準）並轉換爲臺灣常用詞彙
    T2HK,

    //t2hk.json Traditional Chinese to Traditional Chinese (Hong Kong Standard) 繁體到香港繁體（香港小學學習字詞表標準）
    T2S,

    //t2s.json Traditional Chinese to Simplified Chinese 繁體到簡體
    T2TW,

    //t2tw.json Traditional Chinese to Traditional Chinese (Taiwan Standard) 繁體臺灣正體
    T2JP,

    //t2jp.json Traditional Chinese Characters (Kyūjitai) to New Japanese Kanji (Shinjitai) 繁體到日本漢字
    TW2S,

    //tw2s.json Traditional Chinese (Taiwan Standard) to Simplified Chinese 臺灣正體到簡體
    TW2T,

    //tw2t.json Traditional Chinese (Taiwan standard) to Traditional Chinese 臺灣正體到繁體
    TW2SP;

    val value: String
        //tw2sp.json Traditional Chinese (Taiwan Standard) to Simplified Chinese with Mainland Chinese idiom 繁體（臺灣正體標準）到簡體並轉換爲中國大陸常用詞彙
        get() {
            return when(this) {
                HK2S -> "hk2s.json"
                HK2T -> "hk2t.json"
                JP2T -> "jp2t.json"
                S2HK -> "s2hk.json"
                S2T -> "s2t.json"
                S2TW -> "s2tw.json"
                S2TWP -> "s2twp.json"
                T2HK -> "t2hk.json"
                T2S -> "t2s.json"
                T2TW -> "t2tw.json"
                T2JP -> "t2jp.json"
                TW2S -> "tw2s.json"
                TW2T -> "tw2t.json"
                TW2SP -> "tw2sp.json"
                else -> "s2t.json"
            }
        }
}