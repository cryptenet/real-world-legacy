package com.cryptenet.library_core.extensions

fun <T : Enum<T>> T.validateToNull(): T? =
    if (this.name == "UNKNOWN") {
        null
    } else {
        this
    }
