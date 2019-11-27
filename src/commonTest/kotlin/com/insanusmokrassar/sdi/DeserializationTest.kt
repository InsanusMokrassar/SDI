package com.insanusmokrassar.sdi

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.test.*

interface ControllerAPI {
    fun showUp()
}
interface ServiceAPI {
    val names: List<String>
}

@Serializable
class Controller(@ContextualSerialization val service: ServiceAPI) : ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.names}\"")
    }
}
@Serializable
class BusinessService(override val names: List<String>) : ServiceAPI

@ImplicitReflectionSerializer
class DeserializationTest {
    @Test
    fun test_that_simple_config_correctly_work() {
        val names = arrayOf("nameOne", "nameTwo")
        val input = """
            {
                "service": [
                    "com.insanusmokrassar.sdi.BusinessService",
                    {
                        "names": ${names.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                    }
                ],
                "controller": [
                    "com.insanusmokrassar.sdi.Controller",
                    {
                        "service": "service"
                    }
                ]
            }
        """.trimIndent()
        val module = Json.plain.parse(Module.serializer(), input)
        (module["controller"] as ControllerAPI)
        val controller = (module["controller"] as Controller)
        assertEquals(names.toList(), controller.service.names)
    }
}
