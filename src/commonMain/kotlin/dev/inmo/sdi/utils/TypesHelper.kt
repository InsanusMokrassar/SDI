package dev.inmo.sdi.utils

import kotlin.reflect.KClass
import kotlin.reflect.KType

internal expect fun resolveKClassByPackageName(packageName: String): KClass<*>

internal expect val <T : Any> KClass<T>.supertypes: List<KType>

internal val KClass<*>.superclasses: Set<KClass<*>>
    get() {
        val subclasses = mutableSetOf<KClass<*>>()
        val leftToVisit = supertypes.mapNotNull { it.classifier as? KClass<*> }.toMutableList()
        while (leftToVisit.isNotEmpty()) {
            val top = leftToVisit.removeAt(0)
            if (subclasses.add(top)) {
                leftToVisit.addAll(
                    top.superclasses
                )
            }
        }
        return subclasses
    }
