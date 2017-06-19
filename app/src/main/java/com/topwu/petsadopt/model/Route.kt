package com.topwu.petsadopt.model

import com.google.gson.annotations.SerializedName

data class Route(val legs: List<Leg>,
                 @SerializedName("overview_polyline") val polyline: Polyline,
                 val bounds: Bounds)