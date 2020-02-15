# SDI

It is simple (in comparison with other systems) DI, which aim to be compatible and predictable. This library was created
with aim on difficult systems with opportunity to reconfugiry most parts of behaviours without recompilation of code.

## Platforms support

* [x] JVM
* [ ] JS
* [ ] Native

## Required environment

To use this library you will need two things:

* Json serializer
* Json config

Unfortunately, currently not supported other formats (due to
[issue in Kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/issues/615))

## How to implement

Currently you can connect repository JCenter:

```groovy
repositories {
    // ...
    jcenter()
    // ...
}
```

and implement it like here:

```groovy
dependencies {
    implementation "com.insanusmokrassar:sdi:$sdi_version"
}
```

Last version shown here: [ ![Download](https://api.bintray.com/packages/insanusmokrassar/InsanusMokrassar/sdi/images/download.svg) ](https://bintray.com/insanusmokrassar/InsanusMokrassar/sdi/_latestVersion)

## Format of config

Full examples of usage you can find in [tests](src/commonTest/kotlin/com/insanusmokrassar/sdi). In two words, there are
a few rules for constructing of config:

* Config root is an Json Object
* Config root names will be used as dependency names
* In the config dependency names can be used everywhere
* In places, where dependency will be injected, must be used `@ContextualSerializer` annotation or `@Serializer(ContextSerializer::class)`

### Examples

Lets imagine, that we have several interfaces and classes:

```kotlin
package com.example

// ... //

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
```

Here there is common way to create all of this directly:

```kotlin
val service = BusinessService(listOf("One", "Two"))
val controller = Controller(service)
```

And with config for this library:

```json
{
  "service": [
    "com.example.BusinessService",
    {
      "names": ["One", "Two"]
    }
  ],
  "controller": [
    "com.example.Controller",
    {
      "service": "service"
    }
  ]
}
```

Kotlin code will be:

```kotlin
val module = loadModule(input)
val businessService = (module["service"] as BusinessService)
```

Here `input` is a json of configuration.

List example you can find in
[this test](https://git.insanusmokrassar.com/InsanusMokrassar/SDI/src/master/src/commonTest/kotlin/com/insanusmokrassar/sdi/ListTest.kt).
Besides, usually you can create objects inside of places where expected something like dependency injection directly. In
this case config will look like:

```json
{
  "controller": [
    "com.example.Controller",
    {
      "service": [
        "com.example.BusinessService",
        {
          "names": ["One", "Two"]
        }
      ]
    }
  ]
}
```

More expanded example you can find in
[suitable test](https://git.insanusmokrassar.com/InsanusMokrassar/SDI/src/master/src/commonTest/kotlin/com/insanusmokrassar/sdi/SimpleCustomObjectTest.kt#L63).

