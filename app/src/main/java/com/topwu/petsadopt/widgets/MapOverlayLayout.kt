package com.topwu.petsadopt.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.topwu.petsadopt.util.MapsUtil
import java.util.ArrayList

open class MapOverlayLayout<V : MarkerView> constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    protected val markersList: MutableList<V> = ArrayList()

    protected var currentPolyline: Polyline? = null

    protected lateinit var googleMap: GoogleMap
    protected lateinit var polyLines: List<LatLng>

    protected fun addMarker(view: V) {
        markersList.add(view)
        addView(view)
    }

    protected fun removeMarker(view: V) {
        markersList.remove(view)
        removeView(view)
    }

    fun showAllMarkers() {
        markersList.forEach { it.show() }
    }

    fun hideAllMarkers() {
        markersList.forEach { it.hide() }
    }

    open fun showMarker(position: Int) {
        markersList[position].show()
    }

    fun refresh(position: Int, point: Point) {
        markersList[position].refresh(point)
    }

    fun setupMap(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    fun refresh() {
        val projection = googleMap.projection
        markersList.forEachIndexed { index, v ->
            refresh(index, projection.toScreenLocation(v.latLng()))
        }
    }

    fun setOnCameraIdleListener(listener: GoogleMap.OnCameraIdleListener?) {
        googleMap.setOnCameraIdleListener(listener)
    }

    fun setOnCameraMoveListener(listener: GoogleMap.OnCameraMoveListener) {
        googleMap.setOnCameraMoveListener(listener)
    }

    fun moveCamera(latLngBounds: LatLngBounds) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 150))
    }

    fun animateCamera(bounds: LatLngBounds) {
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, MapsUtil.DEFAULT_MAP_PADDING)
        googleMap.animateCamera(cameraUpdate)
    }

    fun getCurrentLatLng(): LatLng {
        return LatLng(googleMap.cameraPosition.target.latitude, googleMap.cameraPosition.target.longitude)
    }

    fun addPolyline(polyLines: ArrayList<LatLng>) {
        this.polyLines = polyLines
        val options = PolylineOptions()

        polyLines.forEachIndexed { i, latLng ->
            if (i > 0) {
                options.add(polyLines[i - 1], latLng).width(10f).color(Color.RED).geodesic(true)
            }

        }
        currentPolyline = googleMap.addPolyline(options)
    }

    fun removeCurrentPolyline() {
        currentPolyline?.remove()
    }
}