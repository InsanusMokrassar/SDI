package com.insanusmokrassar.sdi

import com.insanusmokrassar.sdi.utils.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

class Module(vararg pairs: Pair<String, Any>) : Map<String, Any> by mutableMapOf(*pairs)

private class SubDependencyResolver<T : Any>(
    val kclass: KClass<T>,
    private val parentResolver: DependencyResolver<*>
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("SubDependencyResolver")
    private val dependencies
        get() = parentResolver.dependencies

    override fun deserialize(decoder: Decoder): T = parentResolver.deserialize(decoder) as T

    override fun serialize(encoder: Encoder, obj: T) {
        val key = dependencies.keys.first { key ->
            parentResolver.dependencies[key] === obj
        }
        encoder.encodeString(key)
    }

    fun registerInModuleBuilder(builder: SerializersModuleBuilder) = builder.contextual(kclass, this)
}
private class DependencyResolver<T : Any>(
    val kclass: KClass<T>,
    private val externalDependencyResolver: (String) -> Any?
) : KSerializer<T> {
    private val mutDependencies = mutableMapOf<String, T>()
    val dependencies: Map<String, T>
        get() = mutDependencies
    override val descriptor: SerialDescriptor = StringDescriptor.withName("DependencyResolver")

    val subResolvers = kclass.allSubclasses.map {
        SubDependencyResolver(it, this)
    }

    fun registerNewDependency(name: String, value: T) {
        mutDependencies[name] = value
    }

    fun tryRegisterNewDependency(name: String, value: Any): Boolean {
        return (value as? T) ?.also { registerNewDependency(name, value) } != null
    }

    override fun deserialize(decoder: Decoder): T {
        val dependencyName = decoder.decodeString()
        return mutDependencies[dependencyName]
            ?: (externalDependencyResolver(dependencyName) as? T) ?.also { registerNewDependency(dependencyName, it) }
            ?: throw IllegalArgumentException("Can't resolve dependency for dependency name $dependencyName")
    }

    override fun serialize(encoder: Encoder, obj: T) {
        val key = mutDependencies.keys.first { key ->
            mutDependencies[key] === obj
        }
        encoder.encodeString(key)
    }
}

@ImplicitReflectionSerializer
private class DependenciesHolderAndResolver(
    sourceMap: Map<String, Any>
) : KSerializer<Any> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("DependenciesHolderAndResolver")

    private val typesSerializers = mutableMapOf<KClass<*>, DependencyResolver<*>>()

    private var mapper = Mapper()
    private var module = SerializersModule {}
        set(value) {
            field = value
            mapper = Mapper(module)
        }
    private fun <T: Any> addModuleSerialization(dependencyResolver: DependencyResolver<T>) {
        module += SerializersModule {
            dependencyResolver.subResolvers.forEach { resolver ->
                module.getContextual(resolver.kclass) ?: resolver.registerInModuleBuilder(this)
            }
            contextual(dependencyResolver.kclass, dependencyResolver)
        }
    }
    val dependenciesMap = sourceMap.map { (k, v) ->
        var resolved = false
        lateinit var actualValue: Any
        k to {
            if (!resolved) {
                actualValue = resolveDependency(k, v)
                resolved = true
            }
            actualValue
        }
    }.toMap()

    private fun Map<String, Any>.initDependencies() {
        (keys).forEach { paramKey ->
            val paramValue = get(paramKey)
            if (paramValue is String) {
                dependenciesMap[paramValue] ?.invoke()
            }
        }
    }

    private fun resolveDependency(
        dependencyName: String,
        value: Any
    ): Any {
        val v = when (value) {
            is Map<*, *> -> {
                mapper.unmap(
                    resolveSerializerByPackageName(dependencyName),
                    value.toCommonMap().also { it.initDependencies() }
                ) as Any
            }
            is List<*> -> {
                val packageName = value.firstOrNull() as? String ?: return value
                val arguments = (value.getOrNull(1) as? Map<*, *>) ?.toCommonMap() ?: emptyMap()
                mapper.unmap(
                    resolveSerializerByPackageName(packageName),
                    arguments.also { it.initDependencies() }
                ) as Any
            }
            is String -> dependenciesMap[value] ?.invoke() ?: return value
            else -> value
        }
        (typesSerializers[v::class] ?:let {
            val kclass = v::class
            val resolver = DependencyResolver(kclass) { dependencyName ->
                dependenciesMap[dependencyName]
            }
            typesSerializers[kclass] = resolver
            addModuleSerialization(resolver)
            resolver
        }).also {
            it.tryRegisterNewDependency(dependencyName, v)
        }
        return v
    }

    override fun deserialize(decoder: Decoder): Any {
        val dependencyName = decoder.decodeString()
        return dependenciesMap[dependencyName] ?.invoke() ?: throw IllegalArgumentException("Can't resolve unknown dependency $dependencyName")
    }

    override fun serialize(encoder: Encoder, obj: Any) {
        val key = dependenciesMap.keys.first {
            dependenciesMap[it] ?.invoke() === obj
        }
        encoder.encodeString(key)
    }
}

@Serializer(Module::class)
@ImplicitReflectionSerializer
object ModuleSerializer : KSerializer<Module> {
    private val internalSerializer = HashMapSerializer(StringSerializer, PolymorphicSerializer(Any::class))
    override val descriptor: SerialDescriptor
        get() = internalSerializer.descriptor
    override fun deserialize(decoder: Decoder): Module {
        val json = JsonObjectSerializer.deserialize(decoder)
        val sourceMap = json.adaptForMap()
        val deserializer = DependenciesHolderAndResolver(sourceMap)
        val dependencies = deserializer.dependenciesMap.map { (key, valueGetter) -> key to valueGetter() }
        return Module(*dependencies.toTypedArray())
    }

    override fun serialize(encoder: Encoder, obj: Module) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
