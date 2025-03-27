package com.example.myweatherapp_kotlin

import android.util.Log
import org.jetbrains.annotations.Debug

data class WeatherData (
    var condition: String? = null,
    var currTemp: Int = 0,
    var min: Int = 0,
    var max: Int = 0,
    var locationName: String? = null,
    var locationLat: Double = 0.0,
    var locationLon: Double = 0.0){

    @Override
    override fun toString(): String {
        return "$locationName : $currTemp°, $condition"
    }
}