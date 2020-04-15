package com.insanusmokrassar.sdi

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModuleBuilder

internal val nonStrictJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    serializeSpecialFloatingPointValues = true
}

@ImplicitReflectionSerializer
private val ModuleDeserializerStrategyWithNullOptional = ModuleDeserializerStrategy()

@ImplicitReflectionSerializer
fun Json.loadModule(
    json: String,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = parse(if (moduleBuilder != null) ModuleDeserializerStrategy(moduleBuilder) else ModuleDeserializerStrategyWithNullOptional, json)

@ImplicitReflectionSerializer
fun loadModule(
    json: String,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
): Module = nonStrictJson.loadModule(json, moduleBuilder)
