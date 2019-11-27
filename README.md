# SDI

It is simple (in comparison with other systems) DI, which aim to be compatible and predictable.

## Availability platforms

* [x] JVM
* [ ] JS
* [ ] Native

## Required environment

To use this library you will need two things:

* Json serializer
* Json config

Unfortunately, currently not supported other formats (due to
[issue in Kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/issues/615))

## Rules

Full examples of usage you can find in [tests](src/commonTest/kotlin/com/insanusmokrassar/sdi). In two words, there are
a few rules for constructing of config:

* Config root is an Json Object
* Config root names will be used as dependency names
* In the config dependency names can be used everywhere
* In places, where dependency will be injected, must be used `@ContextualSerializer` annotation or `@Serializer(ContextSerializer::class)`
