package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.json.*

fun JsonPrimitive.adaptForMap(): Any = doubleOrNull ?: floatOrNull ?: longOrNull ?: intOrNull ?: booleanOrNull ?: content
fun JsonArray.adaptForMap(): List<Any> {
    return mapNotNull {
        when (it) {
            is JsonObject -> it.adaptForMap()
            is JsonArray -> it.adaptForMap()
            is JsonPrimitive -> it.adaptForMap()
        }
    }
}
fun JsonObject.adaptForMap(): Map<String, Any> {
    return keys.mapNotNull {
        val value = (
            getArrayOrNull(it) ?.adaptForMap()
                ?: getObjectOrNull(it) ?.adaptForMap()
                ?: getPrimitiveOrNull(it) ?.adaptForMap()
            ) ?: return@mapNotNull null
        it to value
    }.toMap()
}
