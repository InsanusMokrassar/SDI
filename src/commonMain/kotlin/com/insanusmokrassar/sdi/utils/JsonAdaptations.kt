package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*

private typealias PackageOrOtherDependencyNamePair = Pair<String?, String?>

private fun JsonElement.resolvePackageName(currentKey: String, otherDependenciesKeys: Set<String>): PackageOrOtherDependencyNamePair {
    return when (this) {
        is JsonPrimitive -> contentOrNull ?.let {
            if (it in otherDependenciesKeys) {
                null to it
            } else {
                it to null
            }
        } ?: throw IllegalArgumentException("Value on dependency name \"$currentKey\" is invalid: provided $this, but expected package name or other dependency name string")
        is JsonObject -> return currentKey to null
        is JsonArray -> return getPrimitive(0).contentOrNull ?.let { it to null } ?: throw IllegalArgumentException("Value on first argument of dependency value must be its package as a string, but was provided ${get(0)}")
    }
}

@ImplicitReflectionSerializer
internal fun createModuleBasedOnConfigRoot(jsonObject: JsonObject): Json {
    lateinit var caches: Map<String, () -> Any>
    lateinit var jsonStringFormat: Json
    caches = jsonObject.keys.map { key ->
        key to callback@{
            val elemValue = jsonObject.get(key) ?: throw IllegalStateException("Value for key $key must be provided, but was not")

            val packageName: String = elemValue.resolvePackageName(key, jsonObject.keys).let { (packageName, otherDependencyName) ->
                when {
                    packageName != null -> packageName
                    otherDependencyName != null -> return@callback caches.getValue(otherDependencyName).invoke()
                    else -> throw IllegalStateException("Internal error: can't resolve other dependency name and package name for key $key")
                }
            }
            val argumentsObject = when (elemValue) {
                is JsonPrimitive -> {
                    elemValue.contentOrNull ?.let { _ ->
                        JsonObject(emptyMap())
                    } ?: throw IllegalArgumentException("Value on dependency name \"$key\" is invalid: provided $elemValue, but expected package name or other dependency name string")
                }
                is JsonObject -> {
                    elemValue
                }
                is JsonArray -> {
                    if (elemValue.size > 1) {
                        elemValue.getObject(1)
                    } else {
                        JsonObject(emptyMap())
                    }
                }
            }

            val serializer = resolveSerializerByPackageName(packageName)
            return@callback jsonStringFormat.fromJson(serializer, argumentsObject) as Any
        }
    }.toMap()

    val keysToPackages: Map<String, String> = jsonObject.mapNotNull { (key, element) ->
        val packageName = element.resolvePackageName(key, jsonObject.keys).first ?: return@mapNotNull null
        key to packageName
    }.toMap()

    return Json(
        context = SerializersModule {
            keysToPackages.values.forEach {
                val kclass = resolveKClassByPackageName(it)

                try {
                    DependencyResolver(this, kclass) {
                        caches.getValue(it).invoke()
                    }
                } catch (e: SerializationException) {
                    // here we are thinking that already registered
                }
            }
        }
    ).also {
        jsonStringFormat = it
    }
}
