package dev.inmo.sdi.utils

import kotlin.reflect.KClass
import kotlin.reflect.KType

actual fun resolveKClassByPackageName(packageName: String): KClass<*> = Class.forName(packageName).kotlin
internal actual val <T : Any> KClass<T>.supertypes: List<KType>
    get() = supertypes