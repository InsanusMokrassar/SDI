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
        val controllerName = "controller"
        val input = """
            {
                "service": [
                    "${BusinessService::class.qualifiedName}",
                    {
                        "names": ${names.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                    }
                ],
                "$controllerName": [
                    "${Controller::class.qualifiedName}",
                    {
                        "service": "service"
                    }
                ]
            }
        """.trimIndent()
        val module = Json.plain.parse(Module.serializer(), input)
        (module[controllerName] as ControllerAPI)
        val controller = (module["controller"] as Controller)
        assertEquals(names.toList(), controller.service.names)
    }
}
