package com.insanusmokrassar.sdi

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json

internal val nonStrictJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    serializeSpecialFloatingPointValues = true
    useArrayPolymorphism = true
}

@ImplicitReflectionSerializer
fun Json.loadModule(json: String): Module = parse(ModuleDeserializerStrategy, json)

@ImplicitReflectionSerializer
fun loadModule(json: String): Module = nonStrictJson.loadModule(json)
