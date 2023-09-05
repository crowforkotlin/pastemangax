package com.crow.base.tools.extensions.ks

import kotlinx.serialization.SerializationStrategy
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter

internal class SerializationStrategyConverter<T>(
  private val contentType: MediaType,
  private val saver: SerializationStrategy<T>,
  private val serializer: Serializer
) : Converter<T, RequestBody> {
  override fun convert(value: T & Any) = serializer.toRequestBody(contentType, saver, value)
}
