package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

@InternalSerializationApi
fun <T : Any> SerializersModuleBuilder.optionalContextual(
    kClass: KClass<T>, kSerializer: KSerializer<T>
) = try {
    contextual(kClass, kSerializer)
    true
} catch (e: SerializerAlreadyRegisteredException) {
    false
}

@InternalSerializationApi
inline fun <reified T : Any> SerializersModuleBuilder.optionalContextual(
    kSerializer: KSerializer<T>
) = optionalContextual(T::class, kSerializer)