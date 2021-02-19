package dev.inmo.sdi

import kotlinx.serialization.InternalSerializationApi
import kotlin.test.assertEquals

@InternalSerializationApi
fun testModuleSerialization(
    module: Module
) {
    val serializedModule = loadModule(nonStrictJson.encodeToString(DefaultModuleSerializer, module))
    module.forEach { (key, value) ->
        assertEquals(value, serializedModule.getValue(key))
    }
}
