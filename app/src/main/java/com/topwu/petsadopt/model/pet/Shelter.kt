package com.topwu.petsadopt.model.pet

data class Shelter(val shelterId: Int,
                   val name: String,
                   val address: String,
                   val tel: Tel,
                   val latitude: Double,
                   val longitude: Double)