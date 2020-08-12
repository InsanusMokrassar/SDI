package com.insanusmokrassar.sdi

import kotlin.reflect.KClass
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModuleBuilder
import java.io.File
import java.io.InputStream

@ImplicitReflectionSerializer
fun Json.loadModule(
    stream: InputStream,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = loadModule(stream.reader().readText(), *additionalClassesToInclude, moduleBuilder = moduleBuilder)

@ImplicitReflectionSerializer
fun loadModule(
    stream: InputStream,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = nonStrictJson.loadModule(stream, *additionalClassesToInclude, moduleBuilder = moduleBuilder)

@ImplicitReflectionSerializer
fun Json.loadModule(
    file: File,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = loadModule(file.inputStream(), *additionalClassesToInclude, moduleBuilder = moduleBuilder)

@ImplicitReflectionSerializer
fun loadModule(
    file: File,
    vararg additionalClassesToInclude: KClass<*>,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = nonStrictJson.loadModule(file, *additionalClassesToInclude, moduleBuilder = moduleBuilder)
