package com.insanusmokrassar.sdi

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlin.test.*

interface Simple_ControllerAPI {
    fun showUp()
}
interface Simple_ServiceAPI {
    val names: List<String>
}

@Serializable
class Simple_Controller(@ContextualSerialization val service: Simple_ServiceAPI) : Simple_ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.names}\"")
    }
}
@Serializable
class Simple_BusinessService(override val names: List<String>) : Simple_ServiceAPI

@ImplicitReflectionSerializer
class SimpleTest {
    @Test
    fun test_that_simple_config_correctly_work() {
        val names = arrayOf("nameOne", "nameTwo")
        val controllerName = "controller"
        val input = """
            {
                "service": [
                    "${Simple_BusinessService::class.qualifiedName}",
                    {
                        "names": ${names.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                    }
                ],
                "$controllerName": [
                    "${Simple_Controller::class.qualifiedName}",
                    {
                        "service": "service"
                    }
                ]
            }
        """.trimIndent()
        val module = Json.plain.parse(Module.serializer(), input)
        (module[controllerName] as Simple_ControllerAPI)
        val controller = (module["controller"] as Simple_Controller)
        assertEquals(names.toList(), controller.service.names)
    }
}
