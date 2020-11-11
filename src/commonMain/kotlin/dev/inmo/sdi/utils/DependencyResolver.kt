package dev.inmo.sdi.utils

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

internal object AlreadyRegisteredException : Exception()

internal class DependencyResolver<T : Any>(
    serialModuleBuilder: SerializersModuleBuilder,
    kClass: KClass<T>,
    private val formatterGetter: () -> Json,
    private val dependencyGetter: (String) -> Any
) : KSerializer<T> {
    @InternalSerializationApi
    private val originalSerializer: KSerializer<T> = kClass.serializerOrNull() ?: ContextualSerializer(kClass)
    private val objectsCache = mutableMapOf<String, T>()
    @InternalSerializationApi
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

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): T {
        val decoded = decoder.decodeSerializableValue(JsonElement.serializer())
        return when {
            decoded is JsonPrimitive && decoded.contentOrNull != null -> decoded.content.let { dependencyName ->
                @Suppress("UNCHECKED_CAST")
                (dependencyGetter(dependencyName) as T).also {
                    objectsCache[dependencyName] = it
                }
            }
            decoded is JsonArray -> {
                val serializer = resolveSerializerByPackageName(decoded[0].jsonPrimitive.content)
                @Suppress("UNCHECKED_CAST")
                formatterGetter().decodeFromJsonElement(serializer, decoded[1]) as T
            }
            else -> formatterGetter().decodeFromJsonElement(originalSerializer, decoded)
        }
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: T) {
        objectsCache.keys.firstOrNull {
            objectsCache[it] === value
        } ?.also { dependencyName ->
            encoder.encodeString(dependencyName)
        } ?: originalSerializer.serialize(encoder, value)
    }
}
