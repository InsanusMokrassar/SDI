package dev.inmo.sdi

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
expect annotation class SDIIncluded(val customNames: Array<String> = [])

internal expect fun getClassesForIncludingInSDI(): List<Pair<KClass<*>, List<String>>>

@Suppress("NOTHING_TO_INLINE")
@InternalSerializationApi
private inline fun <T : Any> KClass<T>.includeInBuilder(
    builder: SerializersModuleBuilder
) = builder.contextual(this, serializer())

@InternalSerializationApi
fun SerializersModuleBuilder.includeClassesForSDI() {
    getClassesForIncludingInSDI().forEach { (kclass, _) ->
        kclass.includeInBuilder(this)
    }
}
