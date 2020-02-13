package com.insanusmokrassar.sdi

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json

@ImplicitReflectionSerializer
fun loadModule(json: String): Module = Json.nonstrict.parse(Module.serializer(), json)
