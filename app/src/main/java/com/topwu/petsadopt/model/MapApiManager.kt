package com.topwu.petsadopt.model

import com.google.android.gms.maps.model.LatLng
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

class MapApiManager private constructor() {
    private val ORIGIN = "origin"
    private val DESTINATION = "destination"

    private val PETS = "pets"

    companion object {
        val instance: MapApiManager by lazy { MapApiManager() }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor()).build()
    }

    fun getRoute(start: LatLng, end: LatLng, callback: Callback) {
        val urlBuilder = HttpUrl.parse(RestConstants.BASE_URL).newBuilder()
        urlBuilder.addQueryParameter(ORIGIN, start.latitude.toString() + "," + start.longitude)
        urlBuilder.addQueryParameter(DESTINATION, end.latitude.toString() + "," + end.longitude)

        val url = urlBuilder.build()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(callback)
    }

    fun getPets(callback: Callback) {
        val urlBuilder = HttpUrl.parse(RestConstants.PETS_URL).newBuilder()
        urlBuilder.addPathSegment(PETS)

        val url = urlBuilder.build()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(callback)
    }
}