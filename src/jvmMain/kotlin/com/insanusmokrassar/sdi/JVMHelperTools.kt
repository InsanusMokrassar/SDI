package com.insanusmokrassar.sdi

import kotlinx.io.InputStream
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import java.io.File

@ImplicitReflectionSerializer
fun Json.loadModule(stream: InputStream) = loadModule(stream.reader().readText())

@ImplicitReflectionSerializer
fun loadModule(stream: InputStream) = Json.nonstrict.loadModule(stream.reader().readText())

@ImplicitReflectionSerializer
fun Json.loadModule(file: File) = loadModule(file.inputStream())

@ImplicitReflectionSerializer
fun loadModule(file: File) = Json.nonstrict.loadModule(file.inputStream())
