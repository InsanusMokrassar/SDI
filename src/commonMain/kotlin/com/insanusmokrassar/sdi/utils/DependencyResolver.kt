package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializerAlreadyRegisteredException
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

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
        PolymorphicSerializer(kClass)
    }
    private val objectsCache = mutableMapOf<String, T>()
    override val descriptor: SerialDescriptor = originalSerializer.descriptor

    init {
        serialModuleBuilder.apply {
            contextual(kClass, this@DependencyResolver)
            polymorphic(kClass, originalSerializer)
            kClass.allSubclasses.forEach { currentKClass ->
                try {
                    DependencyResolver(serialModuleBuilder, currentKClass, formatterGetter, dependencyGetter)
                } catch (e: SerializerAlreadyRegisteredException) {
                    // ok
                }
            }
        }
    }

    override fun deserialize(decoder: Decoder): T {
        val decoded = decoder.decodeSerializableValue(JsonElementSerializer)
        return when {
            decoded is JsonPrimitive && decoded.contentOrNull != null -> decoded.content.let { dependencyName ->
                (dependencyGetter(dependencyName) as T).also {
                    objectsCache[dependencyName] = it
                }
            }
            decoded is JsonArray -> {
                val serializer = resolveSerializerByPackageName(decoded.getPrimitive(0).content)
                formatterGetter().fromJson(serializer, decoded[1]) as T
            }
            else -> formatterGetter().fromJson(originalSerializer, decoded)
        }
    }

    override fun serialize(encoder: Encoder, obj: T) {
        objectsCache.keys.firstOrNull {
            objectsCache[it] === obj
        } ?.also { dependencyName ->
            encoder.encodeString(dependencyName)
        } ?: originalSerializer.serialize(encoder, obj)
    }
}
