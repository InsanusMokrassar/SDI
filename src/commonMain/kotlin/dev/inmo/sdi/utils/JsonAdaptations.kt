package dev.inmo.sdi.utils

import dev.inmo.sdi.getClassesForIncludingInSDI
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

private typealias PackageOrOtherDependencyNamePair = Pair<String?, String?>

private val namesToTheirClasses = getClassesForIncludingInSDI().flatMap {
    (it.second + it.first.qualifiedName!!).map { name ->
        name to it.first.qualifiedName!!
    }
}.toMap()

private fun JsonElement.resolvePackageName(currentKey: String, otherDependenciesKeys: Set<String>): PackageOrOtherDependencyNamePair {
    return when (this) {
        is JsonPrimitive -> contentOrNull ?.let {
            if (it in otherDependenciesKeys) {
                null to it
            } else {
                it to null
            }
        } ?: throw IllegalArgumentException("Value on dependency name \"$currentKey\" is invalid: provided $this, but expected package name or other dependency name string")
        is JsonObject -> if (currentKey in otherDependenciesKeys) {
            null to currentKey
        } else {
            (namesToTheirClasses[currentKey] ?: currentKey) to null
        }
        is JsonArray -> return get(0).jsonPrimitive.contentOrNull ?.let { (namesToTheirClasses[it] ?: it) to null } ?: throw IllegalArgumentException("Value on first argument of dependency value must be its package as a string, but was provided ${get(0)}")
    }
}

@InternalSerializationApi
internal fun createModuleBasedOnConfigRoot(
    jsonObject: JsonObject,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null,
    baseContext: SerializersModule,
    vararg additionalClassesToInclude: KClass<*>
): Json {
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
                        elemValue[1].jsonObject
                    } else {
                        JsonObject(emptyMap())
                    }
                }
            }

            val serializer = resolveSerializerByPackageName(packageName)
            return@callback jsonStringFormat.decodeFromJsonElement(serializer, argumentsObject) as Any
        }
    }.toMap()

    val keysToPackages: Map<String, String> = jsonObject.mapNotNull { (key, element) ->
        val packageName = element.resolvePackageName(key, jsonObject.keys).first ?: return@mapNotNull null
        key to packageName
    }.toMap()

    val context = baseContext.overwriteWith(
        SerializersModule {
            keysToPackages.values.forEach {
                val kclass = resolveKClassByPackageName(it)

                try {
                    DependencyResolver(
                        this,
                        kclass,
                        { jsonStringFormat }
                    ) {
                        caches.getValue(it).invoke()
                    }
                } catch (e: AlreadyRegisteredException) {
                    // here we are thinking that already registered
                }
            }
            additionalClassesToInclude.forEach {
                try {
                    DependencyResolver(
                        this,
                        it,
                        { jsonStringFormat }
                    ) {
                        caches.getValue(it).invoke()
                    }
                } catch (e: AlreadyRegisteredException) {
                    // here we are thinking that already registered
                }
            }
            if (moduleBuilder != null) {
                moduleBuilder()
            }
        }
    )
    return Json {
        useArrayPolymorphism = true
        serializersModule = context
    }.also {
        jsonStringFormat = it
    }
}
