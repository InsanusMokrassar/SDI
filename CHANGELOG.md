# SDI changelogs

## 0.4.1

* `Serialization`: `1.1.0-RC` -> `1.1.0`

## 0.4.0

* Fix of [#6](https://github.com/InsanusMokrassar/SDI/issues/6)
* Fix of [#7](https://github.com/InsanusMokrassar/SDI/issues/7)

## 0.4.0-rc2

* `Kotlin`: `1.4.21` -> `1.4.30`
* `Serialization`: `1.0.1` -> `1.1.0-RC`

## 0.4.0-rc

**ALL PROJECT HAS MIGRATED FROM PACKAGE `com.insanusmokrassar` TO `dev.inmo`**

* `Kotlin`: `1.3.72` -> `1.4.21`
* `Serialization`: `0.20.0` -> `1.0.1`
* New annotation `@SDIIncluded` has been added
* Now `KlassIndex` is used to get `SDIIncluded` things
* Now `Module` class is `Serializable` and is able to be deserialized from `Json` (unfortunately, currently is not
  supported serialization)


## 0.3.1

* `Kotlin`: `1.3.71` -> `1.3.72`
* Add `SerializersModuleBuilder#optionalContextual` for more comfortable usage of `contextual`
* All `loadModule` functions now able to retrieve additional classes to inspect inside of serializer of module

## 0.3.0

* Versions:
    * `Kotlin`: `1.3.70` -> `1.3.71`
* Common:
    * Now it is possible to pass own lambda with receiver `SerializersModuleBuilder` to customize deserialization
    * Now it will correctly resolve objects which was not previously registered
    * By default, for modules loading will be used context from `Json`, passed as receiver
    
## 0.2.0

* `Kotlin`: `1.3.61` -> `1.3.70`
* `Kotlin Serialization`: `0.14.0` -> `0.20.0`

### 0.1.2

* All reflection support methods are internal
* Added `loadModule` functions for more useful working with modules loading
* `Module` now is not serializable and can be created only via `loadModule` functions (at least, for some time)

### 0.1.1

* Added opportunity to create objects inside of config:
    * Now it is possible to construct object without usage of dependency on `@ContextualSerialization` place
