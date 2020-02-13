package com.insanusmokrassar.sdi.utils

import kotlin.reflect.KClass

actual fun resolveKClassByPackageName(packageName: String): KClass<*> = Class.forName(packageName).kotlin
