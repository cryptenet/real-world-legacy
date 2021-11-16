package com.cryptenet.library_core.extensions

fun Int.validateToNull(allowZero: Boolean = false): Int? =
    if (this == 0 && !allowZero) {
        null
    } else {
        this
    }
