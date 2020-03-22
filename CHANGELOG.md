# SDI changelogs

## 0.2.0

* `Kotlin`: `1.3.61` -> `1.3.70`
* `Kotlin Serialization`: `0.14.0` -> `0.20.0`

## 0.1.0

### 0.1.2

* All reflection support methods are internal
* Added `loadModule` functions for more useful working with modules loading
* `Module` now is not serializable and can be created only via `loadModule` functions (at least, for some time)

### 0.1.1

* Added opportunity to create objects inside of config:
    * Now it is possible to construct object without usage of dependency on `@ContextualSerialization` place
