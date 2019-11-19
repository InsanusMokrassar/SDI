package com.insanusmokrassar.sdi.utils

fun Map<*, *>.toCommonMap() = mapNotNull { (k, v) ->
    (k as? String ?: return@mapNotNull null) to (v ?: return@mapNotNull null)
}.toMap()
