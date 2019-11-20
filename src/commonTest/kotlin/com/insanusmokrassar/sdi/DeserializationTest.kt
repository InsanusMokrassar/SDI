package com.insanusmokrassar.sdi

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

interface ControllerAPI {
    fun showUp()
}
interface ServiceAPI {
    val name: String
}

@Serializable
class Controller(@ContextualSerialization private val service : ServiceAPI) : ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.name}\"")
    }
}
@Serializable
class BusinessService : ServiceAPI {
    @Transient
    override val name = "Example of business name"
}

@ImplicitReflectionSerializer
class DeserializationTest {
    @Test
    fun test_that_simple_config_correctly_work() {
        val input = """
            {
                "service": [
                    "com.insanusmokrassar.sdi.BusinessService"
                ],
                "controller": [
                    "com.insanusmokrassar.sdi.Controller",
                    {
                        "service": "service"
                    }
                ]
            }
        """.trimIndent()
        val module = Json.plain.parse(ModuleSerializer, input)
        (module["controller"] as ControllerAPI).showUp()
    }
}
