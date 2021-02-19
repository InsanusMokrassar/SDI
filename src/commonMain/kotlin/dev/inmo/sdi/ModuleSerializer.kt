package dev.inmo.sdi

import dev.inmo.sdi.utils.*
import dev.inmo.sdi.utils.resolveKClassByPackageName
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@InternalSerializationApi
private fun <T : Any> T.serialize(encoder: Encoder) = (
    this::class.serializer() as KSerializer<T>
).serialize(encoder, this)

private fun JsonElement.detectType(valueKey: String) = when (this) {
    is JsonObject -> this["type"] ?.jsonPrimitive ?.contentOrNull
    is JsonPrimitive -> contentOrNull
    is JsonArray -> getOrNull(0) ?.jsonPrimitive ?.contentOrNull
} ?: valueKey

@InternalSerializationApi
private data class TypeSerializer<T : Any>(
    val serializersModule: SerializersModuleBuilder,
    private val kClass: KClass<T>,
    private val otherDependencyResolver: (String) -> Any?
) : KSerializer<T> {
    private val deserializedByLink = mutableMapOf<T, String>()
    private val jsonElementSerializer = JsonElement.serializer()
    @InternalSerializationApi
    private val originalSerializer = kClass.serializer()
    override val descriptor: SerialDescriptor
        get() = jsonElementSerializer.descriptor

    init {
        fun <T : Any> KClass<T>.contextual() {
            serializersModule.optionalContextual(this, this@TypeSerializer as KSerializer<T>)
        }
        serializersModule.contextual(kClass, this)
        kClass.superclasses.forEach {
            it.contextual()
        }
    }

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): T {
        return when (val element = jsonElementSerializer.deserialize(decoder)) {
            is JsonPrimitive -> (otherDependencyResolver(element.content) as T).also {
                deserializedByLink[it] = element.content
            }
            else -> ((decoder as? JsonDecoder) ?.json ?: nonStrictJson).decodeFromJsonElement(
                originalSerializer,
                when (element) {
                    is JsonArray -> element[1].jsonObject
                    else -> element
                }
            )
        }
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: T) {
        deserializedByLink[value] ?.also {
            encoder.encodeSerializableValue(JsonPrimitive.serializer(), JsonPrimitive(it))
        } ?: value.serialize(encoder)
    }

    @InternalSerializationApi
    fun forceSerialization(json: Json, value: T) = json.encodeToJsonElement(originalSerializer, value).let { encoded ->
        when (encoded) {
            is JsonObject -> JsonObject(
                encoded + ("type" to JsonPrimitive(value::class.qualifiedName))
            )
            is JsonArray -> JsonArray(
                listOf(JsonPrimitive(value::class.qualifiedName)) + encoded
            )
            else -> encoded
        }
    }
}

internal data class SerializationContext(
    val json: Json,
    val keysSerializers: Map<String, KSerializer<*>>
) {
    @InternalSerializationApi
    fun <T : Any> serialize(key: String, value: T) = (keysSerializers.getValue(key) as TypeSerializer<T>).let {
        it.forceSerialization(json, value)
    }
}

@Serializer(Module::class)
class ModuleSerializer(
    private val moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null,
    private vararg val additionalClassesToInclude: KClass<*>
) : KSerializer<Module> {
    private val jsonObjectSerializer = JsonObject.serializer()
    override val descriptor: SerialDescriptor = jsonObjectSerializer.descriptor

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Module {
        val json = jsonObjectSerializer.deserialize(decoder)
        lateinit var newFormat: Json

        val cacheMap = mutableMapOf<String, Any>()
        val serializers = mutableMapOf<String, KSerializer<*>>()
        val dependencyResolver: (String) -> Any? = {
            cacheMap[it] ?: newFormat.decodeFromJsonElement(
                serializers.getValue(it),
                json.getValue(it)
            )
        }

        val newSerializersModule = decoder.serializersModule.overwriteWith(
            SerializersModule {
                moduleBuilder ?.invoke(this)
                json.forEach { (key, value) ->
                    val kclass = resolveKClassByPackageName(value.detectType(key))
                    serializers[key] = TypeSerializer(this, kclass, dependencyResolver)
                }
                additionalClassesToInclude.forEach {
                    TypeSerializer(this, it, dependencyResolver)
                }
            }
        )

        newFormat = Json((decoder as? JsonDecoder) ?.json ?: nonStrictJson) {
            serializersModule = newSerializersModule
        }

        return Module(
            json.mapNotNull { (key) ->
                key to (dependencyResolver(key) ?: return@mapNotNull null)
            }.toMap(),
            SerializationContext(newFormat, serializers.toMap())
        )
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Module) {
        val serialContext = value.serialContext
        jsonObjectSerializer.serialize(
            encoder,
            JsonObject(
                value.map { (key, data) ->
                    key to serialContext.serialize(key, data)
                }.toMap()
            )
        )
    }
}

val DefaultModuleSerializer = ModuleSerializer()
