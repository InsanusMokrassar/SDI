package dev.inmo.sdi

import kotlinx.serialization.*
import kotlin.test.Test
import kotlin.test.assertEquals

interface SimpleCustomObject_ControllerAPI {
    fun showUp()
}
interface SimpleCustomObject_ServiceAPI {
    val names: List<String>
}

@Serializable
class SimpleCustomObject_Controller(@Contextual val service: SimpleCustomObject_ServiceAPI) :
    SimpleCustomObject_ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.names}\"")
    }
}

@Serializable
class SimpleCustomObject_CustomController1(@Contextual val service: SimpleCustomObject_ServiceAPI) :
    SimpleCustomObject_ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.names}\"")
    }
}

@Serializable
class SimpleCustomObject_CustomController2(@Contextual val service: SimpleCustomObject_BusinessService) :
    SimpleCustomObject_ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.names}\"")
    }
}

@Serializable
class SimpleCustomObject_CustomController3(@Contextual val service: SimpleCustomObject_ServiceAPI) :
    SimpleCustomObject_ControllerAPI {
    override fun showUp() {
        println("Inited with name \"${service.names}\"")
    }
}
@Serializable
class SimpleCustomObject_BusinessService(override val names: List<String>) : SimpleCustomObject_ServiceAPI
@Serializable
class SimpleCustomObject_BusinessService1(override val names: List<String>) : SimpleCustomObject_ServiceAPI

class SimpleCustomObjectTest {
    @InternalSerializationApi
    @Test
    fun test_that_simple_config_correctly_work() {
        val names = arrayOf("nameOne", "nameTwo")
        val customNames = arrayOf("customNameOne", "customNameTwo")
        val controllerName = "controller"
        val customController1Name = "controller1"
        val customController2Name = "controller2"
        val customController3Name = "controller3"
        val input = """
            {
                "service": [
                    "${SimpleCustomObject_BusinessService::class.qualifiedName}",
                    {
                        "names": ${names.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                    }
                ],
                "$controllerName": [
                    "${SimpleCustomObject_Controller::class.qualifiedName}",
                    {
                        "service": "service"
                    }
                ],
                "$customController1Name": [
                    "${SimpleCustomObject_CustomController1::class.qualifiedName}",
                    {
                        "service": [
                            "${SimpleCustomObject_BusinessService::class.qualifiedName}",
                            {
                                "names": ${customNames.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                            }
                        ]
                    }
                ],
                "$customController2Name": [
                    "${SimpleCustomObject_CustomController2::class.qualifiedName}",
                    {
                        "service": {
                            "names": ${customNames.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                        }
                    }
                ],
                "$customController3Name": [
                    "${SimpleCustomObject_CustomController3::class.qualifiedName}",
                    {
                        "service": [
                            "${SimpleCustomObject_BusinessService1::class.qualifiedName}",
                            {
                                "names": ${customNames.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()
        val module = loadModule(input)

        (module[controllerName] as SimpleCustomObject_ControllerAPI)
        val controller = (module["controller"] as SimpleCustomObject_Controller)
        assertEquals(names.toList(), controller.service.names)


        (module[customController1Name] as SimpleCustomObject_ControllerAPI)
        val customController1 = (module[customController1Name] as SimpleCustomObject_CustomController1)
        assertEquals(customNames.toList(), customController1.service.names)


        (module[customController2Name] as SimpleCustomObject_ControllerAPI)
        val customController2 = (module[customController2Name] as SimpleCustomObject_CustomController2)
        assertEquals(customNames.toList(), customController2.service.names)
    }
}
