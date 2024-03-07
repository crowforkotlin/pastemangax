package com.crow.base.tools.extensions

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.RequestBody
import org.json.JSONObject

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/extensions
 * @Time: 2023/3/19 0:44
 * @Author: CrowForKotlin
 * @Description: JsonExt
 * @formatter:on
 **************************/

/**
 * @Description: KS Json扩展
 * @author lei , edit by crowforkotlin
 * @date 2021/11/4 3:58 下午
 */
/*
val baseJson = Json {

//    classDiscriminator = "code"           // 多态序列化的类描述符属性的名称
//    prettyPrintIndent = "    "            // 指定打印缩进字符串

    // 是否编码默认值
    encodeDefaults = true

    // 忽略未知 key 不抛出异常
    ignoreUnknownKeys = true

    // 是否使用宽松模式
    isLenient = true

    // 是否允许将 key-value 转换为 数组
    allowStructuredMapKeys = false

    // 是否对打印的 json 格式化
    prettyPrint = true

    // 非空类型为空或找不到对应枚举时使用默认值
    coerceInputValues = false

    // 将多态序列化为默认数组格式
    useArrayPolymorphism = false

    // 是否取消对特殊浮点值的规范
    allowSpecialFloatingPointValues = false
}

*/
/**
 * 使用 [json] 以及 [deserializer] 将 [String] 解析为 [T] 数据实体
 * 转换失败返回 `null`
 *//*

inline fun <reified T> toTypeEntity(value: String?, json: Json = baseJson, deserializer: DeserializationStrategy<T>? = null): T? {
    return when {
        value.isNullOrBlank() -> null
        null != deserializer -> json.decodeFromString(deserializer, value)
        else -> json.decodeFromString(value)
    }
}

*/
/**
 * 使用 [json] 以及 [serializer] 将数据实体 [T] 转换为 [String]
 * > 转换失败返回 `""`
 *//*

inline fun <reified T> toJson(value: T?, json: Json = baseJson, serializer: SerializationStrategy<T>? = null): String {
    return when {
        null == value -> ""
        null != serializer -> json.encodeToString(serializer, value)
        else -> json.encodeToString(value)
    }
}

*/
/** 将 [JSONObject] 转换为 [RequestBody] 并返回 *//*

fun JSONObject.toJsonRequestBody(): RequestBody {
    return toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
}
*/

/*
* @Description: Moshi扩展
* @author CrowForKotlin
* @date 2021/11/4 3:58 下午
* */
val baseMoshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

inline fun <reified T> toTypeEntity(value: Any?, moshi: Moshi = baseMoshi): T? {
    if (value == null) return null
    return if (value is String) moshi.adapter(T::class.java).fromJson(value) else moshi.adapter(T::class.java).fromJsonValue(value)
}

inline fun <reified T> toJson(value: T & Any, moshi: Moshi = baseMoshi): String {
    return moshi.adapter(T::class.java).toJson(value)
}

/**
 * ⦁ Gson 扩展
 *
 * ⦁ 2023-09-05 01:56:41 周二 上午
 */


/*
val baseGson = Gson()

inline fun <reified T> String.toTypeEntity(gson: Gson = baseGson): T {
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T> toTypeEntity(value: Any, gson: Gson = baseGson): T {
    return gson.fromJson(gson.toJson(value), T::class.java)
}


fun toJson(value: Any, gson: Gson = baseGson): String {
    return gson.toJson(*/
/* src = *//*
 value)
}
*/
