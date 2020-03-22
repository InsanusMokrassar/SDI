package com.insanusmokrassar.sdi

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream

@ImplicitReflectionSerializer
fun Json.loadModule(stream: InputStream) = loadModule(stream.reader().readText())

@ImplicitReflectionSerializer
fun loadModule(stream: InputStream) = nonStrictJson.loadModule(stream.reader().readText())

@ImplicitReflectionSerializer
fun Json.loadModule(file: File) = loadModule(file.inputStream())

@ImplicitReflectionSerializer
fun loadModule(file: File) = nonStrictJson.loadModule(file.inputStream())
