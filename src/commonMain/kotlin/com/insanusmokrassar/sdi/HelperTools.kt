package com.insanusmokrassar.sdi

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass

internal val nonStrictJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    serializeSpecialFloatingPointValues = true
}

@ImplicitReflectionSerializer
fun Json.loadModule(
    json: String,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = parse(
    if (moduleBuilder != null) {
        ModuleDeserializerStrategy(moduleBuilder, *additionalClassesToInclude)
    } else {
        ModuleDeserializerStrategy(additionalClassesToInclude = *additionalClassesToInclude)
    },
    json
)

@ImplicitReflectionSerializer
fun loadModule(
    json: String,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = nonStrictJson.loadModule(json, *additionalClassesToInclude, moduleBuilder = moduleBuilder)
