package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KType

@ImplicitReflectionSerializer
expect fun resolveSerializerByPackageName(packageName: String): KSerializer<*>

expect val KClass<*>.currentSupertypes: List<KType>

val KClass<*>.allSubclasses: Set<KClass<*>>
    get() {
        val subclasses = mutableSetOf<KClass<*>>()
        val leftToVisit = currentSupertypes.mapNotNull { it.classifier as? KClass<*> }.toMutableList()
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
