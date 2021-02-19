package dev.inmo.sdi.utils

import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

@InternalSerializationApi
fun <T : Any> SerializersModuleBuilder.optionalContextual(
    kClass: KClass<T>, kSerializer: KSerializer<T>
) = try {
    contextual(kClass, kSerializer)
    true
} catch (e: SerializationException) {
    false
} catch (e: IllegalArgumentException) { // can be a SerializerAlreadyRegisteredException
    false
}
