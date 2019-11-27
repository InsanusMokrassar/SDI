package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.modules.SerializerAlreadyRegisteredException
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

@ImplicitReflectionSerializer
internal class DependencyResolver<T : Any>(
    serialModuleBuilder: SerializersModuleBuilder,
    kClass: KClass<T>,
    private val dependencyGetter: (String) -> Any
) : KSerializer<T> {
    private val originalSerializer: KSerializer<T>? = try {
        resolveSerializerByKClass(kClass)
    } catch (e: Exception) {
        null
    }
    private val objectsCache = mutableMapOf<String, T>()
    override val descriptor: SerialDescriptor = originalSerializer ?.descriptor ?: StringDescriptor.withName("DependencyResolver")

    init {
        serialModuleBuilder.apply {
            contextual(kClass, this@DependencyResolver)
            kClass.allSubclasses.forEach { kClass ->
                try {
                    DependencyResolver(serialModuleBuilder, kClass, dependencyGetter)
                } catch (e: SerializerAlreadyRegisteredException) {
                    // ok
                }
            }
        }
    }

    override fun deserialize(decoder: Decoder): T {
        return try {
            val dependencyName = decoder.decodeString()
            (dependencyGetter(dependencyName) as T).also {
                objectsCache[dependencyName] = it
            }
        } catch (e: Exception) {
            originalSerializer ?.deserialize(decoder) ?: throw IllegalStateException("Can't resolve dependency", e)
        }
    }

    override fun serialize(encoder: Encoder, obj: T) {
        objectsCache.keys.firstOrNull {
            objectsCache[it] === obj
        } ?.also { dependencyName ->
            encoder.encodeString(dependencyName)
        } ?: originalSerializer ?.serialize(encoder, obj) ?: throw IllegalStateException("Can't resolve dependency")
    }
}
