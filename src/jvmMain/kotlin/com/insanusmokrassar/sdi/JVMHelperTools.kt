package com.insanusmokrassar.sdi

import kotlinx.io.InputStream
import kotlinx.serialization.ImplicitReflectionSerializer
import java.io.File

@ImplicitReflectionSerializer
fun loadModule(stream: InputStream) = loadModule(stream.reader().readText())

@ImplicitReflectionSerializer
fun loadModule(file: File) = loadModule(file.inputStream())
