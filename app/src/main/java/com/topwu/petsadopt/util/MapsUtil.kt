package com.topwu.petsadopt.util

import android.util.DisplayMetrics
import android.view.WindowManager
import com.topwu.petsadopt.model.Bounds

object MapsUtil {

    const val DEFAULT_ZOOM = 150
    const val LATITUDE_INCREASE_FACTOR = 1.5
    const val DEFAULT_MAP_PADDING = 30

    fun increaseLatitude(bounds: Bounds): String {
        val southwestLat = bounds.southwest.lat.toDouble()
        val northeastLat = bounds.northeast.lat.toDouble()
        val updatedLat = LATITUDE_INCREASE_FACTOR * Math.abs(northeastLat - southwestLat)
        return (southwestLat - updatedLat).toString()
    }

    fun calculateWidth(windowManager: WindowManager): Int {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    fun calculateHeight(windowManager: WindowManager, paddingBottom: Int): Int {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels - paddingBottom
    }
}