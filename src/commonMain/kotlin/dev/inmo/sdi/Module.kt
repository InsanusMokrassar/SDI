package dev.inmo.sdi

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable(ModuleSerializer::class)
class Module internal constructor(
    base: Map<String, @Contextual Any>,
    internal val serialContext: SerializationContext
) : Map<String, Any> by base