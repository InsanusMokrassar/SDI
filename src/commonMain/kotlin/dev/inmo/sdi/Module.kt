package dev.inmo.sdi

import kotlinx.serialization.Contextual

class Module internal constructor(base: Map<String, @Contextual Any>) : Map<String, Any> by base