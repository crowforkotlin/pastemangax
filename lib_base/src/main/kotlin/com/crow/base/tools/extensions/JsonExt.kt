package com.crow.base.tools.extensions

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/extensions
 * @Time: 2023/3/19 0:44
 * @Author: CrowForKotlin
 * @Description: JsonExt
 * @formatter:on
 **************************/

/**
 * @Description: Json扩展
 * @author lei
 * @date 2021/11/4 3:58 下午
 */
/*
val baseJson = Json {

//    classDiscriminator = "code"           // 多态序列化的类描述符属性的名称
//    prettyPrintIndent = "    "            // 指定打印缩进字符串

    encodeDefaults = true                   // 是否编码默认值
    ignoreUnknownKeys = true                // 忽略未知 key 不抛出异常
    isLenient = true                        // 是否使用宽松模式
    allowStructuredMapKeys = false          // 是否允许将 key-value 转换为 数组
    prettyPrint = true                      // 是否对打印的 json 格式化
    coerceInputValues = false               // 非空类型为空或找不到对应枚举时使用默认值
    useArrayPolymorphism = false            // 将多态序列化为默认数组格式
    allowSpecialFloatingPointValues = false // 是否取消对特殊浮点值的规范
    */
/*serializersModule = SerializersModule {
        contextual(Cookie::class, CookieSerializer)
    }*//*

}

*/
/**
 * 使用 [json] 以及 [deserializer] 将 [String] 解析为 [T] 数据实体
 * 转换失败返回 `null`
 *//*

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> String?.toTypeEntity(json: Json = baseJson, deserializer: DeserializationStrategy<T>? = null): T? {
    return when {
        this.isNullOrBlank() -> null
        null != deserializer -> json.decodeFromString(deserializer, this)
        else -> json.decodeFromString(this)
    }
}
*/

/*
* @Description: Json扩展
* @author CrowForKotlin
* @date 2021/11/4 3:58 下午
* */
val baseMoshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

inline fun <reified T> String?.toTypeEntity(moshi: Moshi = baseMoshi): T? {
    return moshi.adapter(T::class.java).fromJson(this ?: return null)
}
inline fun <reified T> toTypeEntity(value: Any?, moshi: Moshi = baseMoshi): T? {
    if (value == null) return null
    return moshi.adapter(T::class.java).fromJsonValue(value)
}

inline fun <reified T> toJson(value: T & Any, moshi: Moshi = baseMoshi): String {
    return moshi.adapter(T::class.java).toJson(value)
}