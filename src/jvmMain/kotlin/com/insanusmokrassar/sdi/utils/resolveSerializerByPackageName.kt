package com.insanusmokrassar.sdi.utils

import kotlinx.serialization.*

@ImplicitReflectionSerializer
actual fun resolveSerializerByPackageName(packageName: String): KSerializer<*>  = Class.forName(packageName).kotlin.serializer()
