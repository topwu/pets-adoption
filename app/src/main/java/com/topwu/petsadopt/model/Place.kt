package com.topwu.petsadopt.model

import com.google.android.gms.maps.model.LatLng

data class Place(val name: String,
//               @SerializedName("opening_hours") val openingHours: String,
                 val price: Int,
                 val description: String,
                 val lat: Double,
                 val lng: Double,
                 val photo: List<String>) {

    fun getLatLng(): LatLng {
        return LatLng(lat, lng)
    }
}