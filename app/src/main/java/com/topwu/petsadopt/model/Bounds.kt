package com.topwu.petsadopt.model

import com.google.android.gms.maps.model.LatLng

data class Bounds(val northeast: Northeast,
                  val southwest: Southwest) {
    fun getSouthwestLatLng(): LatLng {
        return LatLng(southwest.lat.toDouble(), southwest.lng.toDouble())
    }

    fun getNortheastLatLng(): LatLng {
        return LatLng(northeast.lat.toDouble(), northeast.lng.toDouble())
    }
}