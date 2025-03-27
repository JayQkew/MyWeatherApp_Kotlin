package com.example.myweatherapp_kotlin

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "weather_data_store")

class WeatherDataStore(private val context: Context) {
    private val gson = Gson()
    private val weatherKey = stringPreferencesKey("weather_data")

    // save WeatherData list
    suspend fun saveWeatherData(weatherList: List<WeatherData>){
        val jsonString = gson.toJson(weatherList)
        context.dataStore.edit { preferences ->
            preferences[weatherKey] = jsonString
        }
    }

    // retrieve WeatherData list
    fun getWeatherData(): Flow<MutableList<WeatherData>> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[weatherKey] ?: "[]"
            val type = object : TypeToken<List<WeatherData>>() {}.type
            val weatherList: List<WeatherData> = gson.fromJson(jsonString, type) ?: emptyList()

            weatherList.toMutableList()
        }
    }

    suspend fun getWeatherDataOnce(): MutableList<WeatherData> {
        val preferences = context.dataStore.data.first() // Collects the first value from Flow synchronously
        val jsonString = preferences[weatherKey] ?: "[]"
        val type = object : TypeToken<List<WeatherData>>() {}.type
        return gson.fromJson<List<WeatherData>>(jsonString, type)?.toMutableList() ?: mutableListOf()
    }
}