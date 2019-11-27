package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.*
import kotlin.reflect.KClass

@ImplicitReflectionSerializer
actual fun resolveSerializerByPackageName(packageName: String): KSerializer<*>  = Class.forName(packageName).kotlin.serializer()

@ImplicitReflectionSerializer
actual fun <T : Any> resolveSerializerByKClass(kClass: KClass<T>): KSerializer<T> = kClass.serializer()

actual fun resolveKClassByPackageName(packageName: String): KClass<*> = Class.forName(packageName).kotlin
