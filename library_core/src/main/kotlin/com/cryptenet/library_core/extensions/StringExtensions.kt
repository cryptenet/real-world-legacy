package com.cryptenet.library_core.extensions

import com.cryptenet.library_core.providers.MappingDefaultsProvider

fun String.validateToNull(): String? =
    if (
        this.isBlankOrEmpty() ||
        this == MappingDefaultsProvider.NOT_FOUND ||
        this == MappingDefaultsProvider.NOT_PROVIDED
    ) {
        null
    } else {
        this
    }

fun String.isBlankOrEmpty(): Boolean = this.isBlank() || this.isEmpty()

fun String.capitalizeFirst(): String = this.replaceFirstChar { it.uppercase() }
