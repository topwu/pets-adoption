package com.topwu.petsadopt.model

import com.google.gson.annotations.SerializedName

data class BaliData(@SerializedName("places") val  placeList: List<Place>)