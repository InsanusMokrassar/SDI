package com.insanusmokrassar.sdi

import kotlinx.serialization.ContextualSerialization

class Module internal constructor(base: Map<String, @ContextualSerialization Any>) : Map<String, Any> by base