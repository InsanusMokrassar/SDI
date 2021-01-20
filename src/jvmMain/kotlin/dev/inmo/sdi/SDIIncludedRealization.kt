package dev.inmo.sdi

import com.github.matfax.klassindex.IndexAnnotated
import com.github.matfax.klassindex.KlassIndex
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@IndexAnnotated
actual annotation class SDIIncluded actual constructor(actual val customNames: Array<String>)

internal actual fun getClassesForIncludingInSDI(): List<Pair<KClass<*>, List<String>>> = KlassIndex.getAnnotated(
    SDIIncluded::class
).map {
    it.let {
        it to it.annotations.flatMap { (it as? SDIIncluded) ?.customNames ?.toList() ?: emptyList() }
    }
}
