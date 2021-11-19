package com.cryptenet.rwl_rest.domain.mapper

import com.cryptenet.rwl_rest.domain.model.ParcelableEntity
import com.cryptenet.rwl_rest.domain.model.StorableEntity

abstract class ParcelableStorableEntityMapper
<PARCELABLE : ParcelableEntity, STORABLE : StorableEntity> {
    abstract fun mapToParcelable(storable: STORABLE): PARCELABLE

    abstract fun mapFromParcelable(parcelable: ParcelableEntity): STORABLE

    fun mapToParcelableList(storableList: List<STORABLE>) =
        storableList.map { mapToParcelable(it) }

    fun mapFromParcelableList(parcelableList: List<ParcelableEntity>) =
        parcelableList.map { mapFromParcelable(it) }
}
