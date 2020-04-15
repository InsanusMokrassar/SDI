package com.insanusmokrassar.sdi

import com.insanusmokrassar.sdi.utils.createModuleBasedOnConfigRoot
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModuleBuilder

@ImplicitReflectionSerializer
internal class ModuleDeserializerStrategy(
    private val moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) : DeserializationStrategy<Module> {
    private val internalSerializer = MapSerializer(String.serializer(), ContextSerializer(Any::class))
    override val descriptor: SerialDescriptor
        get() = internalSerializer.descriptor

    override fun deserialize(decoder: Decoder): Module {
        val json = JsonObjectSerializer.deserialize(decoder)
        val jsonSerialFormat = createModuleBasedOnConfigRoot(json, moduleBuilder, decoder.context)
        val resultJson = JsonObject(
            json.keys.associateWith { JsonPrimitive(it) }
        )
        val map = jsonSerialFormat.fromJson(internalSerializer, resultJson)
        return Module(map)
    }

    override fun patch(decoder: Decoder, old: Module): Module = throw UpdateNotSupportedException("Module")
}
