package com.insanusmokrassar.sdi

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModuleBuilder
import java.io.File
import java.io.InputStream

@ImplicitReflectionSerializer
fun Json.loadModule(
    stream: InputStream,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = loadModule(stream.reader().readText(), moduleBuilder)

@ImplicitReflectionSerializer
fun loadModule(
    stream: InputStream,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = nonStrictJson.loadModule(stream, moduleBuilder)

@ImplicitReflectionSerializer
fun Json.loadModule(
    file: File,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = loadModule(file.inputStream(), moduleBuilder)

@ImplicitReflectionSerializer
fun loadModule(
    file: File,
    moduleBuilder: (SerializersModuleBuilder.() -> Unit)? = null
) = nonStrictJson.loadModule(file, moduleBuilder)
