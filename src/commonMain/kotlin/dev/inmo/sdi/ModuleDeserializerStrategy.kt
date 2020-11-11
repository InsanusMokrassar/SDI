package dev.inmo.sdi

import dev.inmo.sdi.utils.createModuleBasedOnConfigRoot
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

internal class ModuleDeserializerStrategy(
    private val moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null,
    private vararg val additionalClassesToInclude: KClass<*>
) : DeserializationStrategy<Module> {
    private val internalSerializer = MapSerializer(String.serializer(), ContextualSerializer(Any::class))
    override val descriptor: SerialDescriptor
        get() = internalSerializer.descriptor

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Module {
        val json = JsonObject.serializer().deserialize(decoder)
        val jsonSerialFormat = createModuleBasedOnConfigRoot(
            json,
            moduleBuilder,
            decoder.serializersModule,
            *additionalClassesToInclude
        )
        val resultJson = JsonObject(
            json.keys.associateWith { JsonPrimitive(it) }
        )
        val map = jsonSerialFormat.decodeFromJsonElement(internalSerializer, resultJson)
        return Module(map)
    }
}
