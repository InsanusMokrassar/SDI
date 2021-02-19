package dev.inmo.sdi

import kotlinx.serialization.*
import kotlin.test.Test
import kotlin.test.assertEquals

interface Simple_ControllerAPI {
    fun showUp()
}
interface Simple_ServiceAPI {
    val names: List<String>
}

@Serializable
data class Simple_Controller(@Contextual val service: Simple_ServiceAPI) : Simple_ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.names}\"")
    }
}
@Serializable
data class Simple_BusinessService(override val names: List<String>) : Simple_ServiceAPI

class SimpleTest {
    @InternalSerializationApi
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
        val module = loadModule(input)
        (module[controllerName] as Simple_ControllerAPI)
        val controller = (module["controller"] as Simple_Controller)
        assertEquals(names.toList(), controller.service.names)

        testModuleSerialization(module)
    }
}
