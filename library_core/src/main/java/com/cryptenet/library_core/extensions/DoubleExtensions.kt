package com.cryptenet.library_core.extensions

fun Double.validateToNull(allowZero: Boolean = false): Double? =
    if (this == 0.0 && !allowZero) {
        null
    } else {
        this
    }
