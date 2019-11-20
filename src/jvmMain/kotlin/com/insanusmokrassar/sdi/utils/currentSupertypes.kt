package com.insanusmokrassar.sdi.utils

import kotlin.reflect.KClass
import kotlin.reflect.KType

actual val KClass<*>.currentSupertypes: List<KType>
    get() = this.supertypes
