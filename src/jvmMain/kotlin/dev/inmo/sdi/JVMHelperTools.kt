package dev.inmo.sdi

import kotlinx.serialization.InternalSerializationApi
import kotlin.reflect.KClass
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModuleBuilder
import java.io.File
import java.io.InputStream


fun Json.loadModule(
    stream: InputStream,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = loadModule(stream.reader().readText(), *additionalClassesToInclude, moduleBuilder = moduleBuilder)


@InternalSerializationApi
fun loadModule(
    stream: InputStream,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = nonStrictJson.loadModule(stream, *additionalClassesToInclude, moduleBuilder = moduleBuilder)


fun Json.loadModule(
    file: File,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = loadModule(file.inputStream(), *additionalClassesToInclude, moduleBuilder = moduleBuilder)


@InternalSerializationApi
fun loadModule(
    file: File,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = nonStrictJson.loadModule(file, *additionalClassesToInclude, moduleBuilder = moduleBuilder)
