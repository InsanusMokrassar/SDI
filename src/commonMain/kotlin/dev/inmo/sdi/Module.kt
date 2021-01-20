package dev.inmo.sdi

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable(ModuleFullSerializer::class)
class Module internal constructor(base: Map<String, @Contextual Any>) : Map<String, Any> by base