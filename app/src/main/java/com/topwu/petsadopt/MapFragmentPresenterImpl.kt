package com.topwu.petsadopt

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.topwu.petsadopt.model.*
import com.topwu.petsadopt.model.pet.Pet
import com.topwu.petsadopt.model.pet.Pets
import com.topwu.petsadopt.model.pet.Shelter
import com.topwu.petsadopt.util.MapsUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class MapFragmentPresenterImpl : MvpPresenterImpl<MapFragmentView>(), MapFragmentPresenter {
    private val LOGTAG = MapFragmentPresenterImpl::class.java.simpleName

    private val mapApiManager by lazy { MapApiManager.instance }
    private val baliDataProvider by lazy { BaliDataProvider.instance }
    private val gson = Gson()

    override fun clearRoute() {
        getView()?.clearPolylinesOnMap()
    }

    override fun drawRoute(source: LatLng, destination: LatLng) {
        mapApiManager.getRoute(source, destination, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val reader = response.body().charStream()
                val routes = gson.fromJson<DirectionsResponse>(reader, DirectionsResponse::class.java).routes

                if (routes.isNotEmpty()) {
                    val route = routes[0]
                    providePolylineToDraw(route.polyline.points)
                    updateMapZoomAndRegion(route.bounds)
                }
            }
        })
    }

    override fun getPets() {
        mapApiManager.getPets(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val reader = response.body().charStream()
                val pets = gson.fromJson<Pets>(reader, Pets::class.java)
                providePetsToUpdate(pets.data)

                val shelters = pets.data.map { it.shelter }
                                        .filterNot { it.latitude == 0.0 && it.longitude == 0.0 }
                                        .distinct()
                provideSheltersToDraw(shelters)
            }
        })
    }

    override fun provideBaliData() {
//        getView()?.provideBaliData(baliDataProvider.providePlacesList())
    }

    override fun onBackPressedWithScene() {
        getView()?.onBackPressedWithScene(baliDataProvider.provideLatLngBoundsForAllPlaces())
    }

    override fun moveMapAndAddMarker() {
//        getView()?.moveMapAndAddMaker(baliDataProvider.provideLatLngBoundsForAllPlaces())
    }

    private fun updateMapZoomAndRegion(bounds: Bounds) {
        bounds.southwest.lat = MapsUtil.increaseLatitude(bounds)
        getView()?.updateMapZoomAndRegion(bounds.getNortheastLatLng(), bounds.getSouthwestLatLng())
    }

    private fun providePolylineToDraw(points: String) {
        getView()?.drawPolylinesOnMap(ArrayList(PolyUtil.decode(points)))
    }

    private fun provideSheltersToDraw(shelters: List<Shelter>) {
        getView()?.provideShelters(shelters)

        val latLngs = ArrayList<LatLng>()
        val builder = LatLngBounds.Builder()

        shelters.map {
            val latLng = LatLng(it.latitude, it.longitude)
            latLngs.add(latLng)
            builder.include(latLng)
        }

        val latLngBounds = builder.build()
        getView()?.moveMapAndAddMaker(latLngs, latLngBounds)
    }

    private fun providePetsToUpdate(pets: List<Pet>) {
        getView()?.providePets(pets)
    }
}
