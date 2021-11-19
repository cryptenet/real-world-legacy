package com.cryptenet.rwl_rest.domain.mapper

import com.cryptenet.rwl_rest.domain.model.ParcelableEntity
import com.cryptenet.rwl_rest.domain.model.TransportableEntity

abstract class ParcelableTransportableEntityMapper
<PARCELABLE : ParcelableEntity, DTO : TransportableEntity> {
    abstract fun mapToParcelable(dto: DTO): PARCELABLE

    abstract fun mapFromParcelable(parcelable: ParcelableEntity): DTO

    fun mapToParcelableList(dtoList: List<DTO>) =
        dtoList.map { mapToParcelable(it) }

    fun mapFromParcelableList(parcelableList: List<ParcelableEntity>) =
        parcelableList.map { mapFromParcelable(it) }
}
