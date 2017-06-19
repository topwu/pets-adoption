package com.topwu.petsadopt

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.topwu.petsadopt.model.pet.Pet
import com.topwu.petsadopt.model.pet.Shelter
import java.util.*

interface MapFragmentView : MvpView {
    fun clearPolylinesOnMap()

    fun drawPolylinesOnMap(polylines: ArrayList<LatLng>)

    fun providePets(pets: List<Pet>)

    fun provideShelters(shelters: List<Shelter>)

    fun onBackPressedWithScene(latLngBounds: LatLngBounds)

    fun moveMapAndAddMaker(latLngs: List<LatLng>, latLngBounds: LatLngBounds)

    fun updateMapZoomAndRegion(northeastLatLng: LatLng, southwestLatLng: LatLng)
}
