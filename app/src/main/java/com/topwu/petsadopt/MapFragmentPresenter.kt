package com.topwu.petsadopt

import com.google.android.gms.maps.model.LatLng

interface MapFragmentPresenter : MvpPresenter<MapFragmentView> {
    fun clearRoute()

    fun drawRoute(source: LatLng, destination: LatLng)

    fun getPets()

    fun provideBaliData()

    fun onBackPressedWithScene()

    fun moveMapAndAddMarker()
}