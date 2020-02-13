package com.insanusmokrassar.sdi

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json

@ImplicitReflectionSerializer
fun Json.loadModule(json: String): Module = parse(ModuleDeserializerStrategy, json)

@ImplicitReflectionSerializer
fun loadModule(json: String): Module = Json.nonstrict.loadModule(json)
