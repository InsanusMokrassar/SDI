package dev.inmo.sdi

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

@InternalSerializationApi
internal val nonStrictJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        includeClassesForSDI()
    }
}

fun Json.loadModule(
    json: String,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = decodeFromString(
    if (moduleBuilder != null) {
        ModuleSerializer(moduleBuilder, *additionalClassesToInclude)
    } else {
        ModuleSerializer(additionalClassesToInclude = *additionalClassesToInclude)
    },
    json
)

@InternalSerializationApi
fun loadModule(
    json: String,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = nonStrictJson.loadModule(json, *additionalClassesToInclude) {
    includeClassesForSDI()
    moduleBuilder ?.invoke(this)
}
