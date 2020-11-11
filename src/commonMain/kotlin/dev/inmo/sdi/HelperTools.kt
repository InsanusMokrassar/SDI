package dev.inmo.sdi

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

internal val nonStrictJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

fun Json.loadModule(
    json: String,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = decodeFromString(
    if (moduleBuilder != null) {
        ModuleDeserializerStrategy(moduleBuilder, *additionalClassesToInclude)
    } else {
        ModuleDeserializerStrategy(additionalClassesToInclude = *additionalClassesToInclude)
    },
    json
)

fun loadModule(
    json: String,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = nonStrictJson.loadModule(json, *additionalClassesToInclude, moduleBuilder = moduleBuilder)
