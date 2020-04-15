package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

internal object AlreadyRegisteredException : Exception()

@ImplicitReflectionSerializer
internal class DependencyResolver<T : Any>(
    serialModuleBuilder: SerializersModuleBuilder,
    kClass: KClass<T>,
    private val formatterGetter: () -> Json,
    private val dependencyGetter: (String) -> Any
) : KSerializer<T> {
    private val originalSerializer: KSerializer<T> = try {
        kClass.serializer()
    } catch (e: Exception) {
        ContextSerializer(kClass)
    }
    private val objectsCache = mutableMapOf<String, T>()
    override val descriptor: SerialDescriptor = originalSerializer.descriptor

    init {
        serialModuleBuilder.apply {
            try {
                contextual(kClass, this@DependencyResolver)
            } catch (e: IllegalArgumentException) {
                throw AlreadyRegisteredException
            }
            kClass.allSubclasses.forEach { currentKClass ->
                try {
                    DependencyResolver(serialModuleBuilder, currentKClass, formatterGetter, dependencyGetter)
                } catch (e: AlreadyRegisteredException) {
                    // ok
                }
            }
        }
    }

    override fun deserialize(decoder: Decoder): T {
        val decoded = decoder.decodeSerializableValue(JsonElementSerializer)
        return when {
            decoded is JsonPrimitive && decoded.contentOrNull != null -> decoded.content.let { dependencyName ->
                @Suppress("UNCHECKED_CAST")
                (dependencyGetter(dependencyName) as T).also {
                    objectsCache[dependencyName] = it
                }
            }
            decoded is JsonArray -> {
                val serializer = resolveSerializerByPackageName(decoded.getPrimitive(0).content)
                @Suppress("UNCHECKED_CAST")
                formatterGetter().fromJson(serializer, decoded[1]) as T
            }
            else -> formatterGetter().fromJson(originalSerializer, decoded)
        }
    }

    override fun serialize(encoder: Encoder, value: T) {
        objectsCache.keys.firstOrNull {
            objectsCache[it] === value
        } ?.also { dependencyName ->
            encoder.encodeString(dependencyName)
        } ?: originalSerializer.serialize(encoder, value)
    }
}
