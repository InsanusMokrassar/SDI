package dev.inmo.sdi

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

interface List_ParentalAPI {
    val services: List<List_ChildAPI>
}
interface List_ChildAPI {
    val names: List<String>
}

@Serializable
data class List_Parent(override val services: List<@Contextual List_ChildAPI>) : List_ParentalAPI
@Serializable
data class List_Child(override val names: List<String>) : List_ChildAPI

class ListTest {
    val servicesNum = 10

    @InternalSerializationApi
    @Test
    fun test_that_simple_config_correctly_work() {
        val names = (0 until servicesNum).map {
            "service$it" to arrayOf("nameOne$it", "nameTwo$it")
        }
        val controllerName = "parent"
        val input = """
            {
                ${names.joinToString { (title, currentNames) -> """
                    "$title": [
                        "${List_Child::class.qualifiedName}",
                        {
                            "names": ${currentNames.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}
                        }
                    ]
                """ }},
                "$controllerName": [
                    "${List_Parent::class.qualifiedName}",
                    {
                        "services": ${names.joinToString(prefix = "[", postfix = "]") { "\"${it.first}\"" }}
                    }
                ]
            }
        """.trimIndent()
        val module = loadModule(input)
        (module[controllerName] as List_ParentalAPI)
        val controller = (module[controllerName] as List_Parent)
        controller.services.forEachIndexed { i, service ->
            assertEquals(names[i].second.toList(), service.names)
        }

        testModuleSerialization(module)
    }
}
