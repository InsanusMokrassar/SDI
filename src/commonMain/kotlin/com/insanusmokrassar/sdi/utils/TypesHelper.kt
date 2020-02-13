package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.*
import kotlin.reflect.KClass

internal expect fun resolveKClassByPackageName(packageName: String): KClass<*>

@ImplicitReflectionSerializer
internal fun <T : Any> resolveSerializerByKClass(kClass: KClass<T>): KSerializer<T> = kClass.serializer()

@ImplicitReflectionSerializer
internal fun resolveSerializerByPackageName(packageName: String): KSerializer<*> = resolveSerializerByKClass(
    resolveKClassByPackageName(packageName)
)

internal val KClass<*>.allSubclasses: Set<KClass<*>>
    get() {
        val subclasses = mutableSetOf<KClass<*>>()
        val leftToVisit = supertypes.mapNotNull { it.classifier as? KClass<*> }.toMutableList()
        while (leftToVisit.isNotEmpty()) {
            val top = leftToVisit.removeAt(0)
            if (subclasses.add(top)) {
                leftToVisit.addAll(
                    top.allSubclasses
                )
            }
        }
        return subclasses
    }
