package com.topwu.petsadopt.widgets

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.topwu.petsadopt.R

class PulseOverlayLayout constructor(context: Context, attrs: AttributeSet? = null) : MapOverlayLayout<PulseMarkerView>(context, attrs) {

    val ANIMATION_DELAY_FACTOR = 100
    var scaleAnimationDelay = 100

    private var startMarker: PulseMarkerView? = null
    private var finishMarker: PulseMarkerView? = null

    init {
        View.inflate(context, R.layout.pulse_wrapper_layout, this)
    }

    fun setupMarkers(point: Point, latLng: LatLng) {
        startMarker = PulseMarkerView(context, latLng, point)
        finishMarker = PulseMarkerView(context, latLng, point)
    }

    fun removeStartMarker() {
        if (startMarker != null) {
            removeMarker(startMarker as PulseMarkerView)
        }
    }

    fun removeFinishMarker() {
        if (finishMarker != null) {
            removeMarker(finishMarker as PulseMarkerView)
        }
    }

    fun addStartMarker(latLng: LatLng) {
        val marker = createPulseMarkerView(latLng)
        marker.updatePulseViewLayoutParams(googleMap.projection.toScreenLocation(latLng))
        addMarker(marker)
        marker.show()

        startMarker = marker
    }

    fun addFinishMarker(latLng: LatLng) {
        val marker = createPulseMarkerView(latLng)
        marker.updatePulseViewLayoutParams(googleMap.projection.toScreenLocation(latLng))
        addMarker(marker)
        marker.show()

        finishMarker = marker
    }

    private fun createPulseMarkerView(latLng: LatLng): PulseMarkerView {
        val point = googleMap.projection.toScreenLocation(latLng)
        return PulseMarkerView(context, latLng, point)
    }

    private fun createPulseMarkerView(position: Int, point: Point, latLng: LatLng): PulseMarkerView {
        val pulseMarkerView = PulseMarkerView(context, latLng, point, position)
        addMarker(pulseMarkerView)
        return pulseMarkerView
    }

    fun createAndShowMarker(position: Int, latLng: LatLng) {
        val marker = createPulseMarkerView(position, googleMap.projection.toScreenLocation(latLng), latLng)
        marker.showWithDelay(scaleAnimationDelay)
        scaleAnimationDelay += ANIMATION_DELAY_FACTOR
    }

    override fun showMarker(position: Int) {
        markersList[position].pulse()
    }

    fun drawStartAndFinishMarker() {
        addStartMarker(polyLines[0])
        addFinishMarker(polyLines.last())
        setOnCameraIdleListener(null)
    }

    fun onBackPressed(latLngBounds: LatLngBounds) {
        moveCamera(latLngBounds)
        removeStartAndFinishMarkers()
        removeCurrentPolyline()
        showAllMarkers()
        refresh()
    }

    private fun removeStartAndFinishMarkers() {
        removeStartMarker()
        removeFinishMarker()
    }
}