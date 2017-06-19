package com.topwu.petsadopt.model

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.topwu.petsadopt.App
import java.io.InputStreamReader

class BaliDataProvider private constructor(context: Context) {
    private val LOGTAG = BaliDataProvider::class.java.simpleName

    private val JSON_PATH = "bali.json"
    private val URL = "https://pets-adopt.appspot.com"

    companion object {
        val instance : BaliDataProvider by lazy {
            val context = App.instance
            BaliDataProvider(context)
        }
    }

    private val baliData: BaliData by lazy {
        val inputStream = context.assets.open(JSON_PATH)
        val reader = InputStreamReader(inputStream)
        Gson().fromJson<BaliData>(reader, BaliData::class.java)
    }

    fun provideLatLngBoundsForAllPlaces(): LatLngBounds {
        val builder = LatLngBounds.Builder()
        baliData.placeList.forEach {
            builder.include(LatLng(it.lat, it.lng))
        }
        return builder.build()
    }

    fun providePlacesList(): List<Place> {
        return baliData.placeList
    }

    fun getLatByPosition(position: Int): Double {
        return baliData.placeList[position].lat
    }

    fun getLngByPosition(position: Int): Double {
        return baliData.placeList[position].lng
    }
}