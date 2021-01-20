package dev.inmo.sdi

import dev.inmo.sdi.utils.createModuleBasedOnConfigRoot
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

internal class ModuleDeserializerStrategy(
    private val moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null,
    private vararg val additionalClassesToInclude: KClass<*>
) : DeserializationStrategy<Module> {
    constructor() : this(null)

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

@Serializer(Module::class)
internal class ModuleFullSerializer(
    private val moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null,
    private vararg val additionalClassesToInclude: KClass<*>
) : KSerializer<Module>,
    DeserializationStrategy<Module> by ModuleDeserializerStrategy(moduleBuilder, *additionalClassesToInclude) {
    constructor() : this(null)

    override fun serialize(encoder: Encoder, value: Module) = throw NotImplementedError("Currently there is no support for serialization of modules")
}
