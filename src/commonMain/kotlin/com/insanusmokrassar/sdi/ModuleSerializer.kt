package com.insanusmokrassar.sdi

import com.insanusmokrassar.sdi.utils.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

@ImplicitReflectionSerializer
@Serializable(ModuleSerializer::class)
class Module(base: Map<String, @ContextualSerialization Any>) : Map<String, Any> by base

@ImplicitReflectionSerializer
@Serializer(Module::class)
object ModuleSerializer : KSerializer<Module> {
    private val internalSerializer = HashMapSerializer(StringSerializer, ContextSerializer(Any::class))
    override val descriptor: SerialDescriptor
        get() = internalSerializer.descriptor

    override fun deserialize(decoder: Decoder): Module {
        val json = JsonObjectSerializer.deserialize(decoder)
        val jsonSerialFormat = createModuleBasedOnConfigRoot(json)
        val resultJson = JsonObject(
            json.keys.associateWith { JsonPrimitive(it) }
        )
        val map = jsonSerialFormat.fromJson(internalSerializer, resultJson)
        return Module(map)
    }

    override fun serialize(encoder: Encoder, obj: Module) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
