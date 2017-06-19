package com.topwu.petsadopt.widgets

import android.content.Context
import android.graphics.Point
import android.view.View
import com.google.android.gms.maps.model.LatLng

abstract class MarkerView constructor(context: Context,
                                      val latLng: LatLng,
                                      var point: Point) : View(context) {

    fun lat(): Double {
        return latLng.latitude
    }

    fun lng(): Double {
        return latLng.longitude
    }

    fun point(): Point {
        return point
    }

    fun latLng(): LatLng {
        return latLng
    }

    abstract fun show()
    abstract fun hide()
    abstract fun refresh(point: Point)
}