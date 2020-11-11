package dev.inmo.sdi

import org.atteo.classindex.ClassIndex
import org.atteo.classindex.IndexAnnotated
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@IndexAnnotated
actual annotation class SDIIncluded actual constructor(actual val customNames: Array<String>)

internal actual fun getClassesForIncludingInSDI(): List<Pair<KClass<*>, List<String>>> = ClassIndex.getAnnotated(
    SDIIncluded::class.java
).map {
    it.kotlin.let {
        it to it.annotations.flatMap { (it as? SDIIncluded) ?.customNames ?.toList() ?: emptyList() }
    }
}
